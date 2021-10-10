package cga.exercise.components.geometry.mesh

import cga.exercise.components.geometry.IRenderableContainer
import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.shader.ShaderProgram

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