package openGLOutput.exercise.components.geometry.particle

import openGLOutput.exercise.components.geometry.material.AtlasMaterial
import openGLOutput.exercise.components.geometry.transformable.Transformable

class ParticleSpawner(private val particleBase: Particle, material: AtlasMaterial ,parent: Transformable? = null) : ParticleHolder(mutableListOf<Particle>(), material, parent) {


    fun update(dt: Float){

        val particleIterator = particles.iterator()

        while(particleIterator.hasNext()){

            val particle = particleIterator.next()

            if(particle.update(dt))
                particleIterator.remove()
        }
    }

    fun add(){

        var particleCopy = particleBase.getCopy()

        if(parent != null)
            particleCopy.position.add(parent!!.getWorldPosition())

        particles.add(particleCopy)
    }


}