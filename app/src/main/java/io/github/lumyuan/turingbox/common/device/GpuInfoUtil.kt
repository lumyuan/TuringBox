package io.github.lumyuan.turingbox.common.device

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class GpuInfoUtil : GLSurfaceView.Renderer {
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        glRenderer = gl.glGetString(GL10.GL_RENDERER) //GPU 渲染器
        glVendor = gl.glGetString(GL10.GL_VENDOR) //GPU 供应商
        glVersion = gl.glGetString(GL10.GL_VERSION) //GPU 版本
        glExtensions = gl.glGetString(GL10.GL_EXTENSIONS) //GPU 扩展名
    }

    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {}
    override fun onDrawFrame(gl: GL10) {}

    companion object {
        var glRenderer: String? = null
        var glVendor: String? = null
        var glVersion: String? = null
        var glExtensions: String? = null
    }
}

