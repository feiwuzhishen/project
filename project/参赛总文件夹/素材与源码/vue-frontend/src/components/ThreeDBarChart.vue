<template>
  <div ref="chartContainer" class="three-bar-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

const props = defineProps({
  data: {
    type: Array,
    default: () => [
      { name: '股票', value: 4500, change: 2.5 },
      { name: '基金', value: 3200, change: -1.2 },
      { name: '债券', value: 2800, change: 0.8 },
      { name: '期货', value: 1800, change: 3.1 },
      { name: '黄金', value: 2100, change: 1.5 }
    ]
  }
})

const chartContainer = ref(null)
let scene, camera, renderer, controls, bars = []
let animationId = null

const initChart = () => {
  if (!chartContainer.value || !props.data.length) return

  const width = chartContainer.value.clientWidth
  const height = chartContainer.value.clientHeight

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xffffff)

  camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.position.set(8, 12, 15)
  camera.lookAt(0, 0, 0)

  renderer = new THREE.WebGLRenderer({ antialias: true })
  renderer.setSize(width, height)
  renderer.setPixelRatio(window.devicePixelRatio)
  renderer.shadowMap.enabled = true
  renderer.shadowMap.type = THREE.PCFSoftShadowMap
  chartContainer.value.appendChild(renderer.domElement)

  controls = new OrbitControls(camera, renderer.domElement)
  controls.enableDamping = true
  controls.dampingFactor = 0.05
  controls.minDistance = 10
  controls.maxDistance = 50

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.6)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.8)
  directionalLight.position.set(10, 20, 10)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 2048
  directionalLight.shadow.mapSize.height = 2048
  directionalLight.shadow.camera.near = 0.5
  directionalLight.shadow.camera.far = 50
  directionalLight.shadow.camera.left = -15
  directionalLight.shadow.camera.right = 15
  directionalLight.shadow.camera.top = 15
  directionalLight.shadow.camera.bottom = -15
  scene.add(directionalLight)

  const fillLight = new THREE.DirectionalLight(0xffffff, 0.3)
  fillLight.position.set(-5, 10, -5)
  scene.add(fillLight)

  createFloor()
  createBars()
}

const createFloor = () => {
  const floorGeometry = new THREE.PlaneGeometry(20, 20)
  const floorMaterial = new THREE.MeshStandardMaterial({
    color: 0xf5f5f5,
    roughness: 0.8,
    metalness: 0.1
  })
  const floor = new THREE.Mesh(floorGeometry, floorMaterial)
  floor.rotation.x = -Math.PI / 2
  floor.position.y = 0
  floor.receiveShadow = true
  scene.add(floor)

  const gridHelper = new THREE.GridHelper(20, 20, 0xe0e0e0, 0xf0f0f0)
  gridHelper.position.y = 0.01
  scene.add(gridHelper)
}

const createBars = () => {
  bars.forEach(bar => {
    scene.remove(bar.mesh)
    if (bar.mesh.geometry) bar.mesh.geometry.dispose()
    if (bar.mesh.material) bar.mesh.material.dispose()
  })
  bars = []

  const maxValue = Math.max(...props.data.map(d => d.value))
  const barCount = props.data.length
  const spacing = 2
  const totalWidth = (barCount - 1) * spacing
  const startX = -totalWidth / 2

  props.data.forEach((item, index) => {
    const barHeight = (item.value / maxValue) * 8
    const barWidth = 1.2
    const barDepth = 1.2

    const geometry = new THREE.BoxGeometry(barWidth, barHeight, barDepth)

    const color = new THREE.Color()
    const hue = 0.55 - (index * 0.08)
    color.setHSL(hue, 0.6, 0.55)

    const material = new THREE.MeshStandardMaterial({
      color: color,
      roughness: 0.3,
      metalness: 0.2
    })

    const mesh = new THREE.Mesh(geometry, material)
    mesh.position.x = startX + index * spacing
    mesh.position.y = barHeight / 2
    mesh.position.z = 0
    mesh.castShadow = true
    mesh.receiveShadow = true

    const targetHeight = barHeight
    mesh.scale.y = 0.01

    scene.add(mesh)
    bars.push({ mesh, targetHeight })

    createBarLabel(item.name, mesh.position.x, 0, item.value)
  })
}

const createBarLabel = (text, x, y, z) => {
  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  canvas.width = 256
  canvas.height = 64

  context.fillStyle = 'rgba(255, 255, 255, 0.9)'
  context.fillRect(0, 0, canvas.width, canvas.height)

  context.font = 'bold 32px Arial'
  context.fillStyle = '#333333'
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(text, canvas.width / 2, canvas.height / 2)

  const texture = new THREE.CanvasTexture(canvas)
  const spriteMaterial = new THREE.SpriteMaterial({ map: texture })
  const sprite = new THREE.Sprite(spriteMaterial)
  sprite.position.set(x, y - 0.8, z + 1.5)
  sprite.scale.set(2, 0.5, 1)
  scene.add(sprite)
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  bars.forEach(bar => {
    if (bar.mesh.scale.y < bar.targetHeight) {
      bar.mesh.scale.y += (bar.targetHeight - bar.mesh.scale.y) * 0.08
      bar.mesh.position.y = bar.mesh.scale.y / 2
    }
  })

  controls.update()
  renderer.render(scene, camera)
}

const handleResize = () => {
  if (!chartContainer.value || !camera || !renderer) return

  const width = chartContainer.value.clientWidth
  const height = chartContainer.value.clientHeight

  camera.aspect = width / height
  camera.updateProjectionMatrix()
  renderer.setSize(width, height)
}

const disposeChart = () => {
  if (animationId) {
    cancelAnimationFrame(animationId)
  }

  bars.forEach(bar => {
    if (bar.mesh.geometry) bar.mesh.geometry.dispose()
    if (bar.mesh.material) bar.mesh.material.dispose()
    scene.remove(bar.mesh)
  })
  bars = []

  if (renderer) {
    renderer.dispose()
    if (chartContainer.value && renderer.domElement) {
      chartContainer.value.removeChild(renderer.domElement)
    }
  }

  if (controls) {
    controls.dispose()
  }

  scene = null
  camera = null
  renderer = null
  controls = null
}

onMounted(() => {
  initChart()
  animate()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  disposeChart()
})

watch(() => props.data, () => {
  disposeChart()
  initChart()
}, { deep: true })
</script>

<style scoped>
.three-bar-chart {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border-radius: 8px;
  overflow: hidden;
}
</style>
