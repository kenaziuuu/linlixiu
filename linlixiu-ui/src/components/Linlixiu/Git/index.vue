<template>
  <div>
    <svg-icon icon-class="github" @click="goto" />
  </div>
</template>

<script>
export default {
  name: 'LinlixiuGit',
  data() {
    return {
      url: 'https://github.com/JackloveCheng/Workorder-Managerment-System'
    }
  },
  methods: {
    goto() {
      window.open(this.url)
    }
  }
}
</script>