package openGLOutput.exercise.game

import Evaluator.Evaluator
import Parser.ParserManager
import Parser.ParserToken.Expression
import Parser.ParserToken.File
import Parser.ParserToken.Type
import Parser.ParserToken.Values.ConstantValue
import Parser.ParserToken.Values.DynamicValue
import TypeChecker.TypeChecker
import openGLOutput.exercise.components.camera.Camera
import openGLOutput.exercise.components.geometry.material.Material
import openGLOutput.exercise.components.geometry.mesh.RenderableBase
import openGLOutput.exercise.components.light.*
import openGLOutput.exercise.components.shader.ShaderProgram
import openGLOutput.framework.GLError
import openGLOutput.framework.GameWindow
import openGLOutput.framework.ModelLoader
import org.joml.Math.toRadians
import org.joml.Vector3f
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL11.*
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis

class Scene(private val window: GameWindow) {

    //Shader
    private val mainShader: ShaderProgram = ShaderProgram("assets/shaders/main_vert.glsl", "assets/shaders/main_frag.glsl")

    private var renderables : List<RenderableBase> = listOf()

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

    private var environment : HashMap<String, Expression.Value>? = null

    private val mainFile : File = ParserManager.loadFromDisk("code/App.3dc")

    //scene setup
    init {
        
        //initial opengl state
        glClearColor(0f, 0f, 0f, 1.0f); GLError.checkThrow()

        glEnable(GL_CULL_FACE); GLError.checkThrow()
        glFrontFace(GL_CCW); GLError.checkThrow()
        glCullFace(GL_BACK); GLError.checkThrow()

        glEnable(GL_DEPTH_TEST); GLError.checkThrow()
        glDepthFunc(GL_LESS); GLError.checkThrow()

        camera.translateLocal(Vector3f(0f,0f,4f))

        //type check
        val typeCheckTime = measureTimeMillis {
            TypeChecker().check(mainFile, null, "Init")
        }
        println("-- Typechecking took $typeCheckTime ms--")

        //eval
        val evalTime = measureTimeMillis {
            Evaluator().eval(mainFile,null, "Init")
        }
        println("-- Eval took $evalTime ms--")


        environment = mainFile.globalEnvironment

        val evalEnvironmentTime = measureTimeMillis {
            val renderablesObjects = environment!!["objects"]?.value

            if (renderablesObjects != null && renderablesObjects is DynamicValue.Class) {
                renderables = get3dCodeObjectsAsRenderObj(renderablesObjects)
            }
        }
        println("-- Eval Environment took $evalEnvironmentTime ms--")
    }

    fun get3dCodeObjectsAsRenderObj(listObj : DynamicValue.Class ) : List<RenderableBase>{
        if(listObj.type.name != "List")
            throw Exception("objects must be of type 'List'")

        val returnValue = mutableListOf<RenderableBase>()

        val a = listObj.value["values"]!!.value as DynamicValue.Class
        val b = a.value["array"]!!.value as DynamicValue.Array
        b.value.forEach {
            val tempObject = it.value
            if(tempObject is DynamicValue.Class){
                val meshes = (tempObject.value["_object"]!!.value as DynamicValue.Object).value.meshes
                val renderable = RenderableBase(meshes)
                val position = (tempObject.value["position"]!!.value as DynamicValue.Class).value
                val scale =  (tempObject.value["scale"]!!.value as DynamicValue.Class).value
                val rotation =  (tempObject.value["rotation"]!!.value as DynamicValue.Class).value
                val color =  (tempObject.value["color"]!!.value as DynamicValue.Class).value
                renderable.setPosition(Vector3f(position["x"]!!.value.value as Float,position["y"]!!.value.value as Float,position["z"]!!.value.value as Float))
                renderable.rotateLocal(rotation["x"]!!.value.value as Float,rotation["y"]!!.value.value as Float,rotation["z"]!!.value.value as Float)
                renderable.scaleLocal(Vector3f(scale["x"]!!.value.value as Float,scale["y"]!!.value.value as Float,scale["z"]!!.value.value as Float))
                renderable.emitColor = Vector3f(color["x"]!!.value.value as Float / 255f, color["y"]!!.value.value as Float / 255f, color["z"]!!.value.value as Float / 255f)
                returnValue.add(renderable)
            }
        }
        return returnValue
    }

    var lastTime = 0.5f

    fun render(dt: Float, t: Float) {

        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        //-- main Shader
        pointLightHolder.bind(mainShader,"pointLight")
        spotLightHolder.bind(mainShader,"spotLight", camera.getCalculateViewMatrix())

        //mainShader.setUniform("emitColor", Vector3f(0f,0.5f,1f))

        if(t-lastTime > 0.01f)
            mainShader.setUniform("time", t)

        renderables.forEach {
            it.render(mainShader)
        }

        camera.bind(mainShader, camera.getCalculateProjectionMatrix(), camera.getCalculateViewMatrix())

        if(t-lastTime > 0.01f)
            lastTime = t
    }

    var codeDt = 0.0f
    var updateCount = 0
    var updateSpeed = 0
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

        codeDt += dt
        updateCount++

        if(updateCount >= updateSpeed)
            if(environment != null){

                val args = listOf( Expression.Value(ConstantValue.Float(codeDt)), Expression.Value(ConstantValue.Float(t)))
                Evaluator().eval(mainFile,args, "Update", environment!!)

                val renderablesObjects = environment!!["objects"]?.value

                if (renderablesObjects != null && renderablesObjects is DynamicValue.Class) {
                    renderables = get3dCodeObjectsAsRenderObj(renderablesObjects)
                }
                codeDt = 0.0f
                updateCount = 0
            }
    }

    fun onKey(key: Int, scancode: Int, action: Int, mode: Int) {
        if(environment != null){

            val args = listOf( Expression.Value(ConstantValue.Integer(key)), Expression.Value(ConstantValue.Integer(action)))
            Evaluator().eval(mainFile,args, "OnKey", environment!!)

            val renderablesObjects = environment!!["objects"]?.value

            if (renderablesObjects != null && renderablesObjects is DynamicValue.Class) {
                renderables = get3dCodeObjectsAsRenderObj(renderablesObjects)
            }
        }

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
