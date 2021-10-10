package cga.exercise.components.geometry.gui

import cga.exercise.components.geometry.IRenderableContainer
import cga.exercise.components.geometry.RenderCategory
import cga.exercise.components.shader.ShaderProgram
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import java.util.*
import kotlin.collections.HashMap

class Gui(guiElements: HashMap< String , GuiElement>) : HashMap< String , GuiElement>(guiElements), IRenderableContainer {

    private val guiRenderOrder : List<GuiElement> = guiElements.values.sortedBy { it.zAxisPosition }

    override fun render(cameraMode: List<RenderCategory>, shaderProgram: ShaderProgram){
        shaderProgram.use()

        GL30.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glDisable(GL11.GL_DEPTH_TEST)

        guiRenderOrder.forEach { g ->
            var shouldRender = false

            for(r in cameraMode) {
                if(shouldRender)
                    break

                shouldRender = g.shouldRender.contains(r)
            }

            if(shouldRender)
                g.render(shaderProgram)
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL30.glDisable(GL11.GL_BLEND)


    }

    override fun cleanup() {
        super.entries.forEach { it.value.cleanup() }
    }


}