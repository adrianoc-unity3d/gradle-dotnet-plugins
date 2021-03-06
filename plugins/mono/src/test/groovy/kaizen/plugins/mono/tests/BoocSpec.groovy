package kaizen.plugins.mono.tests

import kaizen.plugins.clr.ClrExecSpec
import Mono
import MonoProvider
import org.gradle.process.ExecResult
import org.gradle.util.ConfigureUtil
import spock.lang.Specification
import spock.lang.Unroll
import kaizen.plugins.mono.compilers.BooCompiler
import kaizen.plugins.mono.Mono
import kaizen.plugins.mono.MonoProvider

class BoocSpec extends Specification {

	@Unroll
	def 'executes booc with the correct command line for #targetFramework'() {
		given:
		def monoProvider = Mock(MonoProvider)
		def mono = Mock(Mono)
		def legacyMono = Mock(Mono)
		def clrExecSpec = Mock(ClrExecSpec)
		def clrExecResult = Mock(ExecResult)

		def compiler = new BooCompiler(monoProvider)
		def sources = [new File('/tmp/a.boo'), new File('/tmp/b.boo')]
		def output = new File('/tmp/file.dll')
		def boocExe = "booc.exe"

		def expectedArgs = []
		expectedArgs.add '-target:library'
		expectedArgs.add "-out:$output.canonicalPath"
		expectedArgs.addAll assemblyReferences.collect { "-r:$it" }
		expectedArgs.addAll sources*.canonicalPath

		when:
		def result = compiler.exec {
			targetFrameworkVersion targetFramework
			outputAssembly output
			references assemblyReferences
			sourceFiles sources
		}

		then:
		_ * monoProvider.runtimeForFrameworkVersion(targetFramework) >> mono
		_ * monoProvider.runtimeForFrameworkVersion('unity') >> legacyMono
		1 * (expectedProfile == 'unity' ? legacyMono : mono).lib(expectedProfile, 'booc.exe') >> boocExe
		1 * mono.exec { ConfigureUtil.configure(it, clrExecSpec) } >> clrExecResult
		1 * clrExecSpec.executable(boocExe)
		1 * clrExecSpec.args(expectedArgs)
		0 * _
		result == clrExecResult

		where:
		targetFramework | expectedProfile | assemblyReferences
		'v3.5'          | 'unity'         | ['System.Xml']
		'v4.0'          | '4.0'           | []
	}

	def 'allows custom executable'() {
		given:
		def monoProvider = Mock(MonoProvider)
		def booc = new BooCompiler(monoProvider)

		when:
		booc.executable = '/my/booc.exe'

		then:
		'/my/booc.exe' == booc.executable
		0 * _

	}
}
