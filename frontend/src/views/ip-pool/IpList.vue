<template>
  <router-view v-if="false" />
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import router from '@/router'

onMounted(() => {
  // Redirect to IpSettings.vue since that's the component we've enhanced
  router.replace('/ip-pool/settings')
})
</script> 