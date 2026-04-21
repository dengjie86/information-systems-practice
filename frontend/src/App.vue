<template>
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style>
.page-enter-active,
.page-leave-active {
  transition: opacity 0.35s ease, transform 0.35s ease;
}

.page-leave-to {
  opacity: 0;
  transform: translateX(-60px);
}

.page-enter-from {
  opacity: 0;
  transform: translateX(60px);
}
</style>
