package openGLOutput.exercise.components.geometry.mesh

import openGLOutput.exercise.components.geometry.IRenderableContainer
import openGLOutput.exercise.components.geometry.RenderCategory
import openGLOutput.exercise.components.shader.ShaderProgram

class RenderableContainer(renderables : HashMap< String ,Renderable>) : HashMap<String, Renderable>(renderables),IRenderableContainer {

    override fun render(cameraMode: List<RenderCategory>, shaderProgram: ShaderProgram) {
        super.entries.forEach { e ->
            var shouldRender = false

            for(r in cameraMode) {
                if(shouldRender)
                    break

                shouldRender = e.value.shouldRender.contains(r)
            }

            if(shouldRender)
                e.value.render(shaderProgram)
        }
    }

    override fun cleanup() {
        super.entries.forEach { it.value.cleanup()}
    }

}