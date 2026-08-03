<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
const emit = defineEmits<{ close: [] }>()
function onKeydown(event: KeyboardEvent) { if (event.key === 'Escape') emit('close') }
onMounted(() => { document.body.classList.add('modal-open'); window.addEventListener('keydown', onKeydown) })
onBeforeUnmount(() => { document.body.classList.remove('modal-open'); window.removeEventListener('keydown', onKeydown) })
</script>
<template><Teleport to="body"><div class="modal-backdrop" @click.self="emit('close')"><section class="modal" role="dialog" aria-modal="true"><button class="modal__close" aria-label="关闭" @click="emit('close')">×</button><slot /></section></div></Teleport></template>
