package kaizen.plugins.unity

import kaizen.plugins.clr.ClrExtension
import kaizen.plugins.clr.ClrPlugin
import kaizen.plugins.unity.internal.DefaultUnityLocator
import kaizen.plugins.unity.internal.ProjectPropertyUnityLocator
import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.internal.os.OperatingSystem
import kaizen.plugins.mono.ProjectExecHandler
import kaizen.plugins.mono.compilers.Mcs
import kaizen.plugins.mono.compilers.BooCompiler

class UnityPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		project.plugins.apply ClrPlugin

		def os = OperatingSystem.current()
		def locator = new ProjectPropertyUnityLocator(project, new DefaultUnityLocator(os))
		def execHandler = new ProjectExecHandler(project)
		def unity = project.extensions.create('unity', Unity, locator, os, execHandler)

		def clr = ClrExtension.forProject(project)
		clr.providers.add(unity)
		clr.compilers.add(new Mcs(unity))
		clr.compilers.add(new BooCompiler(unity))
	}
}
