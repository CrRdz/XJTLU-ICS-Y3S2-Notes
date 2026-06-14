<script setup lang="ts">
import { toasts, confirmState, handleConfirm } from '../utils/ui-feedback'
</script>

<template>
  <!-- Toasts -->
  <div class="toast-stack">
    <TransitionGroup name="toast">
      <div
        v-for="t in toasts"
        :key="t.id"
        class="toast"
        :class="`toast-${t.type}`"
      >
        <div class="toast-accent"></div>
        <div class="toast-message">{{ t.message }}</div>
      </div>
    </TransitionGroup>
  </div>

  <!-- Confirm Dialog -->
  <Teleport to="body">
    <div
      v-if="confirmState.show"
      class="dialog-overlay"
      @click.self="handleConfirm(false)"
    >
      <div class="dialog">
        <div class="dialog-header">
          <h3 class="dialog-title">{{ confirmState.title }}</h3>
        </div>
        <div class="dialog-body">{{ confirmState.message }}</div>
        <div class="dialog-actions">
          <button class="btn-dialog-secondary" @click="handleConfirm(false)">
            Cancel
          </button>
          <button class="btn-dialog-primary" @click="handleConfirm(true)">
            Confirm
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.toast-stack {
  position: fixed;
  top: 24px;
  right: 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
  z-index: 9999;
  pointer-events: none;
}

.toast {
  min-width: 260px;
  max-width: min(420px, calc(100vw - 32px));
  background: #fff;
  border: 1px solid rgba(148, 163, 184, 0.28);
  box-shadow: 0 18px 45px rgba(15, 23, 42, 0.14);
  border-radius: 16px;
  padding: 14px 16px;
  display: flex;
  gap: 12px;
  align-items: flex-start;
  pointer-events: auto;
}

.toast-accent {
  width: 4px;
  border-radius: 999px;
  align-self: stretch;
  background: #5b8def;
}

.toast-success .toast-accent {
  background: #22c55e;
}
.toast-error .toast-accent {
  background: #ef4444;
}
.toast-warning .toast-accent {
  background: #f59e0b;
}

.toast-message {
  font-size: 14px;
  line-height: 1.6;
  color: #1e293b;
  font-family: 'Segoe UI', 'Microsoft YaHei', Arial, sans-serif;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(60px);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(60px);
}

.dialog-overlay {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.38);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  z-index: 10000;
}

.dialog {
  width: min(440px, 100%);
  background: #fff;
  border-radius: 22px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.2);
  border: 1px solid rgba(226, 232, 240, 0.9);
  overflow: hidden;
}

.dialog-header {
  padding: 20px 22px 10px;
}

.dialog-title {
  margin: 0;
  font-size: 18px;
  color: #0f172a;
  font-weight: 700;
}

.dialog-body {
  padding: 0 22px 20px;
  color: #475569;
  font-size: 14px;
  line-height: 1.7;
  font-family: 'Segoe UI', 'Microsoft YaHei', Arial, sans-serif;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 22px 22px;
}

.btn-dialog-secondary,
.btn-dialog-primary {
  border: none;
  border-radius: 999px;
  padding: 10px 18px;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
}

.btn-dialog-secondary {
  background: #eef2f7;
  color: #334155;
}

.btn-dialog-primary {
  background: #5b8def;
  color: #fff;
}
</style>
