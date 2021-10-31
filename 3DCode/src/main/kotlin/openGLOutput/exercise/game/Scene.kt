package openGLOutput.exercise.game

import Evaluator.Evaluator
import Parser.ParserManager
import TypeChecker.TypeChecker
import openGLOutput.exercise.components.camera.Camera
import openGLOutput.exercise.components.light.*
import openGLOutput.exercise.components.shader.ShaderProgram
import openGLOutput.framework.GLError
import openGLOutput.framework.GameWindow
import openGLOutput.framework.ModelLoader
import org.joml.Math.toRadians
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import java.io.File

class Scene(private val window: GameWindow) {

    //Shader
    private val mainShader: ShaderProgram = ShaderProgram("assets/shaders/main_vert.glsl", "assets/shaders/main_frag.glsl")

//-------------------------------------------------------------------------------------------------------------------------------------------------------
    private val pointLightHolder = PointLightHolder( mutableListOf(
        //PointLight(Vector3f(0f,4f,5f), Color(150 * 8 ,245 * 8,255 * 8).toVector3f(), renderables["spaceShipInside"])
        //PointLight(Vector3f(20f,1f,20f),Vector3f(1f,0f,1f))
    ))

    private val spotLightHolder = SpotLightHolder( mutableListOf(
//        SpotLight(Vector3f(0f,1f,0f),Vector3f(1f,1f,1f),  50f, 70f),
//        SpotLight(Vector3f(0f,1f,0f),Vector3f(1f,1f,0.6f),  30f, 90f )
    ))

    // camera
    private val camera = Camera()

//    val mesh = Mesh(
//        floatArrayOf(-1.0f, -1.0f, 1.0f,
//            1.0f, -1.0f, 1.0f,
//            1.0f, 1.0f, 1.0f,
//            -1.0f, 1.0f, 1.0f,
//            -1.0f, -1.0f, -1.0f,
//            1.0f, -1.0f, -1.0f,
//            1.0f, 1.0f, -1.0f,
//            -1.0f, 1.0f, -1.0f),
//        intArrayOf(0, 1, 2, 2, 3, 0,
//            3, 2, 6, 6, 7, 3,
//            7, 6, 5, 5, 4, 7,
//            4, 0, 3, 3, 7, 4,
//            0, 1, 5, 5, 4, 0,
//            1, 5, 6, 6, 2, 1),
//        arrayOf(
//            VertexAttribute(3, GL_FLOAT,12,0)
//        ),
//        Material(
//            Texture2D.invoke("assets/textures/ground_diff.png",true),
//            Texture2D.invoke("assets/textures/ground_emit.png",true),
//            Texture2D.invoke("assets/textures/ground_spec.png",true))
//
//    )
//
//    val cube = RenderableBase(mutableListOf(mesh))

    val cube = ModelLoader.loadModel("assets/textures/untitled.obj",0f,toRadians(180f),0f)!!

    //scene setup
    init {
        
        //initial opengl state
        glClearColor(1f, 0f, 1f, 1.0f); GLError.checkThrow()

        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()

        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        camera.translateLocal(Vector3f(0f,1f,4f))

        val mainFile = ParserManager.loadFromDisk("code/App.3dc")
        TypeChecker().check(mainFile, null)

        println( Evaluator().eval(mainFile,null)?.value)



    }


    var lastTime = 0.5f

    fun render(dt: Float, t: Float) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        //-- main Shader
        pointLightHolder.bind(mainShader,"pointLight")
        spotLightHolder.bind(mainShader,"spotLight", camera.getCalculateViewMatrix())

        mainShader.setUniform("emitColor", Vector3f(0f,0.5f,1f))

        if(t-lastTime > 0.01f)
            mainShader.setUniform("time", t)

        cube.render(mainShader)

        camera.bind(mainShader, camera.getCalculateProjectionMatrix(), camera.getCalculateViewMatrix())

        if(t-lastTime > 0.01f)
            lastTime = t
    }



    fun update(dt: Float, t: Float) {
        val rotationMultiplier = 30f
        val translationMultiplier = 35.0f

        if (window.getKeyState(GLFW_KEY_Q)) {
            camera.rotateLocal(rotationMultiplier * dt, 0.0f, 0.0f)
        }

        if (window.getKeyState(GLFW_KEY_E)) {
            camera.rotateLocal(-rotationMultiplier  * dt, 0.0f, 0.0f)
        }


        if (window.getKeyState ( GLFW_KEY_W)) {
            camera.translateLocal(Vector3f(0.0f, 0.0f, -translationMultiplier * dt))
        }

        if (window.getKeyState ( GLFW_KEY_S)) {
            camera.translateLocal(Vector3f(0.0f, 0.0f, translationMultiplier * dt))
        }

        if (window.getKeyState ( GLFW_KEY_G)) {
            camera.translateLocal(Vector3f(0.0f, 0.0f, translationMultiplier * dt * 10))
        }

        if (window.getKeyState ( GLFW_KEY_A))
            camera.rotateLocal(0.0f, 0.0f, rotationMultiplier* dt)

        if (window.getKeyState ( GLFW_KEY_D))
            camera.rotateLocal(0.0f, 0.0f, -rotationMultiplier* dt)



    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {

    }


    var oldXpos : Double = 0.0
    var oldYpos : Double = 0.0

    fun onMouseMove(xpos: Double, ypos: Double) {

        camera.rotateLocal((oldYpos-ypos).toFloat()/20.0f, (oldXpos-xpos).toFloat()/20.0f, 0f)

        oldXpos = xpos
        oldYpos = ypos
    }

    fun onMouseScroll(xoffset: Double, yoffset: Double) {
        val yoffset = -yoffset.toFloat()

    }

    fun cleanup() {
//        renderables.cleanup()
//        gui.cleanup()

        mainShader.cleanup()
//        guiShader.cleanup()
//        //skyBoxShader.cleanup()
    }


}
