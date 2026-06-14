<script setup lang="ts">
withDefaults(
  defineProps<{
    account?: string
    email?: string
    roleLabel?: string
    hint?: string
  }>(),
  {
    account: '',
    email: '',
    roleLabel: '',
    hint: 'Use this account name when signing in.',
  },
)
</script>

<template>
  <section class="account-panel" aria-label="Current account details">
    <div class="account-panel__header">
      <div>
        <p class="account-panel__eyebrow">Sign-in account</p>
        <h2 class="account-panel__title">Keep this username handy</h2>
      </div>
      <span v-if="roleLabel" class="account-panel__role">{{ roleLabel }}</span>
    </div>

    <div class="account-panel__body">
      <div class="account-panel__identity">
        <span class="account-panel__label">Account</span>
        <strong class="account-panel__value">{{
          account || 'Unavailable'
        }}</strong>
      </div>

      <dl class="account-panel__meta">
        <div class="account-panel__meta-item">
          <dt>Email</dt>
          <dd>{{ email || 'Unavailable' }}</dd>
        </div>
        <div class="account-panel__meta-item">
          <dt>Reminder</dt>
          <dd>{{ hint }}</dd>
        </div>
      </dl>
    </div>
  </section>
</template>

<style scoped>
.account-panel {
  width: min(860px, 100%);
  border-radius: 28px;
  padding: 24px 28px;
  background:
    radial-gradient(
      circle at top right,
      rgba(36, 179, 255, 0.18),
      transparent 28%
    ),
    linear-gradient(135deg, rgba(90, 43, 152, 0.08), rgba(123, 79, 189, 0.16));
  border: 1px solid var(--border-purple);
  box-shadow: 0 18px 48px rgba(90, 43, 152, 0.08);
}

.account-panel__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.account-panel__eyebrow {
  font-size: 0.82rem;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--mid);
  margin-bottom: 6px;
}

.account-panel__title {
  font-size: 1.35rem;
  font-weight: 700;
  color: var(--text);
}

.account-panel__role {
  flex-shrink: 0;
  border-radius: 999px;
  padding: 8px 14px;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(90, 43, 152, 0.14);
  color: var(--deep);
  font-size: 0.88rem;
  font-weight: 700;
}

.account-panel__body {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(0, 1fr);
  gap: 20px;
}

.account-panel__identity,
.account-panel__meta-item {
  border-radius: 20px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid rgba(90, 43, 152, 0.12);
}

.account-panel__label,
.account-panel__meta dt {
  display: block;
  margin-bottom: 8px;
  color: var(--muted);
  font-size: 0.85rem;
  font-weight: 700;
  letter-spacing: 0.03em;
  text-transform: uppercase;
}

.account-panel__value {
  display: inline-block;
  font-size: 1.35rem;
  line-height: 1.3;
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: var(--deep);
  word-break: break-all;
}

.account-panel__meta {
  display: grid;
  gap: 12px;
}

.account-panel__meta dd {
  color: var(--text);
  font-size: 0.96rem;
}

@media (max-width: 720px) {
  .account-panel {
    padding: 20px;
  }

  .account-panel__header,
  .account-panel__body {
    grid-template-columns: 1fr;
    flex-direction: column;
  }

  .account-panel__role {
    align-self: flex-start;
  }

  .account-panel__value {
    font-size: 1.18rem;
  }
}
</style>
