<template>
  <div ref="chartContainer" class="three-pie-chart"></div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import * as THREE from 'three'
import { OrbitControls } from 'three/addons/controls/OrbitControls.js'

const props = defineProps({
  data: {
    type: Array,
    default: () => [
      { name: '股票', value: 35, color: '#4A90D9' },
      { name: '基金', value: 25, color: '#50C878' },
      { name: '债券', value: 20, color: '#FFB347' },
      { name: '黄金', value: 12, color: '#FF6B6B' },
      { name: '其他', value: 8, color: '#9B8FE8' }
    ]
  }
})

const chartContainer = ref(null)
let scene, camera, renderer, controls, slices = []
let animationId = null

const initChart = () => {
  if (!chartContainer.value || !props.data.length) return

  const width = chartContainer.value.clientWidth
  const height = chartContainer.value.clientHeight

  scene = new THREE.Scene()
  scene.background = new THREE.Color(0xffffff)

  camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000)
  camera.position.set(0, 12, 12)
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
  controls.minDistance = 8
  controls.maxDistance = 30
  controls.maxPolarAngle = Math.PI / 2

  const ambientLight = new THREE.AmbientLight(0xffffff, 0.7)
  scene.add(ambientLight)

  const directionalLight = new THREE.DirectionalLight(0xffffff, 0.9)
  directionalLight.position.set(10, 20, 10)
  directionalLight.castShadow = true
  directionalLight.shadow.mapSize.width = 2048
  directionalLight.shadow.mapSize.height = 2048
  scene.add(directionalLight)

  const fillLight = new THREE.DirectionalLight(0xffffff, 0.4)
  fillLight.position.set(-5, 10, -5)
  scene.add(fillLight)

  createFloor()
  createPieSlices()
}

const createFloor = () => {
  const floorGeometry = new THREE.CircleGeometry(12, 64)
  const floorMaterial = new THREE.MeshStandardMaterial({
    color: 0xf8f8f8,
    roughness: 0.9,
    metalness: 0
  })
  const floor = new THREE.Mesh(floorGeometry, floorMaterial)
  floor.rotation.x = -Math.PI / 2
  floor.position.y = -0.01
  floor.receiveShadow = true
  scene.add(floor)
}

const createPieSlices = () => {
  slices.forEach(slice => {
    scene.remove(slice.group)
    slice.group.traverse(child => {
      if (child.geometry) child.geometry.dispose()
      if (child.material) child.material.dispose()
    })
  })
  slices = []

  const total = props.data.reduce((sum, item) => sum + item.value, 0)
  const radius = 4
  const height = 2
  let currentAngle = 0

  props.data.forEach((item, index) => {
    const sliceAngle = (item.value / total) * Math.PI * 2
    const color = new THREE.Color(item.color)

    const sliceGroup = new THREE.Group()
    sliceGroup.position.y = height / 2

    const sliceGeometry = createSliceGeometry(radius, height, currentAngle, sliceAngle)
    const sliceMaterial = new THREE.MeshStandardMaterial({
      color: color,
      roughness: 0.3,
      metalness: 0.1,
      side: THREE.DoubleSide
    })
    const sliceMesh = new THREE.Mesh(sliceGeometry, sliceMaterial)
    sliceMesh.castShadow = true
    sliceMesh.receiveShadow = true
    sliceGroup.add(sliceMesh)

    const edgeGeometry = createEdgeGeometry(radius, height, currentAngle, sliceAngle)
    const edgeMaterial = new THREE.MeshBasicMaterial({
      color: 0xffffff,
      transparent: true,
      opacity: 0.6
    })
    const edgeMesh = new THREE.Mesh(edgeGeometry, edgeMaterial)
    sliceGroup.add(edgeMesh)

    const topGeometry = createTopGeometry(radius, currentAngle, sliceAngle)
    const topMaterial = new THREE.MeshStandardMaterial({
      color: color.clone().lerp(new THREE.Color(0xffffff), 0.2),
      roughness: 0.2,
      metalness: 0.15,
      side: THREE.DoubleSide
    })
    const topMesh = new THREE.Mesh(topGeometry, topMaterial)
    topMesh.position.y = height / 2
    topMesh.castShadow = true
    sliceGroup.add(topMesh)

    const outerEdgeGeometry = createOuterEdgeGeometry(radius, height, currentAngle, sliceAngle)
    const outerEdgeMaterial = new THREE.LineBasicMaterial({
      color: 0x888888,
      transparent: true,
      opacity: 0.4
    })
    const outerEdge = new THREE.Line(outerEdgeGeometry, outerEdgeMaterial)
    sliceGroup.add(outerEdge)

    sliceGroup.rotation.x = -Math.PI / 2
    sliceGroup.scale.y = 0.01

    scene.add(sliceGroup)

    slices.push({
      group: sliceGroup,
      targetScale: 1,
      targetY: height / 2,
      sliceAngle: sliceAngle,
      currentAngle: currentAngle
    })

    createSliceLabel(item.name, item.value, radius, currentAngle, sliceAngle, index)

    currentAngle += sliceAngle
  })
}

const createSliceGeometry = (radius, height, startAngle, endAngle) => {
  const shape = new THREE.Shape()
  shape.moveTo(0, 0)
  shape.absarc(0, 0, radius, startAngle, endAngle, false)
  shape.lineTo(0, 0)
  shape.closePath()

  const extrudeSettings = {
    depth: height,
    bevelEnabled: true,
    bevelThickness: 0.05,
    bevelSize: 0.05,
    bevelSegments: 2
  }

  return new THREE.ExtrudeGeometry(shape, extrudeSettings)
}

const createTopGeometry = (radius, startAngle, endAngle) => {
  const shape = new THREE.Shape()
  shape.moveTo(0, 0)
  shape.absarc(0, 0, radius, startAngle, endAngle, false)
  shape.lineTo(0, 0)
  shape.closePath()

  return new THREE.ShapeGeometry(shape)
}

const createEdgeGeometry = (radius, height, startAngle, endAngle) => {
  const points = []
  points.push(new THREE.Vector3(0, 0, 0))
  points.push(new THREE.Vector3(Math.cos(startAngle) * radius, 0, Math.sin(startAngle) * radius))
  points.push(new THREE.Vector3(Math.cos(startAngle) * radius, height, Math.sin(startAngle) * radius))
  points.push(new THREE.Vector3(0, 0, 0))
  points.push(new THREE.Vector3(Math.cos(endAngle) * radius, 0, Math.sin(endAngle) * radius))
  points.push(new THREE.Vector3(Math.cos(endAngle) * radius, height, Math.sin(endAngle) * radius))

  return new THREE.BufferGeometry().setFromPoints(points)
}

const createOuterEdgeGeometry = (radius, height, startAngle, endAngle) => {
  const curve = new THREE.QuadraticBezierCurve3(
    new THREE.Vector3(Math.cos(startAngle) * radius, 0, Math.sin(startAngle) * radius),
    new THREE.Vector3(Math.cos((startAngle + endAngle) / 2) * (radius + 0.5), height * 0.8, Math.sin((startAngle + endAngle) / 2) * (radius + 0.5)),
    new THREE.Vector3(Math.cos(endAngle) * radius, height, Math.sin(endAngle) * radius)
  )

  return new THREE.BufferGeometry().setFromPoints(curve.getPoints(20))
}

const createSliceLabel = (text, value, radius, startAngle, endAngle, index) => {
  const midAngle = startAngle + endAngle / 2
  const labelRadius = radius * 0.7

  const canvas = document.createElement('canvas')
  const context = canvas.getContext('2d')
  canvas.width = 256
  canvas.height = 96

  context.fillStyle = 'rgba(255, 255, 255, 0.85)'
  context.fillRect(0, 0, canvas.width, canvas.height)
  context.strokeStyle = '#e0e0e0'
  context.lineWidth = 2
  context.strokeRect(1, 1, canvas.width - 2, canvas.height - 2)

  context.font = 'bold 28px Arial'
  context.fillStyle = '#333333'
  context.textAlign = 'center'
  context.textBaseline = 'middle'
  context.fillText(text, canvas.width / 2, canvas.height / 2 - 12)

  context.font = '24px Arial'
  context.fillStyle = '#666666'
  context.fillText(`${value}%`, canvas.width / 2, canvas.height / 2 + 20)

  const texture = new THREE.CanvasTexture(canvas)
  const spriteMaterial = new THREE.SpriteMaterial({
    map: texture,
    transparent: true
  })
  const sprite = new THREE.Sprite(spriteMaterial)
  sprite.position.set(
    Math.cos(midAngle) * labelRadius,
    2.5,
    Math.sin(midAngle) * labelRadius
  )
  sprite.scale.set(2.5, 0.9, 1)
  scene.add(sprite)
}

const animate = () => {
  animationId = requestAnimationFrame(animate)

  slices.forEach((slice, index) => {
    if (slice.group.scale.y < slice.targetScale) {
      const newScale = slice.group.scale.y + (slice.targetScale - slice.group.scale.y) * 0.06
      slice.group.scale.y = newScale

      const progress = (newScale - 0.01) / (1 - 0.01)
      slice.group.position.y = slice.targetY * progress
    }
  })

  if (scene) {
    scene.rotation.y += 0.002
  }

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

  slices.forEach(slice => {
    slice.group.traverse(child => {
      if (child.geometry) child.geometry.dispose()
      if (child.material) {
        if (child.material.map) child.material.map.dispose()
        child.material.dispose()
      }
    })
    scene.remove(slice.group)
  })
  slices = []

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
.three-pie-chart {
  width: 100%;
  height: 100%;
  min-height: 400px;
  border-radius: 8px;
  overflow: hidden;
}
</style>
