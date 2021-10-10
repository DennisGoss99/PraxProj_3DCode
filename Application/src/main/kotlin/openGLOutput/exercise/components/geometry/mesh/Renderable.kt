package openGLOutput.exercise.components.geometry.mesh

import openGLOutput.exercise.components.geometry.RenderCategory

open class Renderable(val shouldRender : List<RenderCategory>, renderable : RenderableBase) : RenderableBase(renderable.meshes, renderable.modelMatrix, renderable.parent)