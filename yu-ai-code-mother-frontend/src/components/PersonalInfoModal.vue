<template>
  <a-modal
    :open="open"
    title="个人信息"
    :confirm-loading="submitting"
    :mask-closable="false"
    @cancel="handleClose"
  >
    <template #footer>
      <a-space>
        <a-button v-if="!isEditMode" @click="handleClose">关闭</a-button>
        <a-button v-if="!isEditMode" type="primary" @click="startEdit">编辑资料</a-button>
        <a-button v-if="isEditMode" @click="cancelEdit">取消编辑</a-button>
        <a-button v-if="isEditMode" type="primary" :loading="submitting" @click="handleSubmit">
          保存
        </a-button>
      </a-space>
    </template>

    <div class="profile-header">
      <a-avatar :src="displayAvatar" :size="72">
        {{ (displayName || 'U').charAt(0) }}
      </a-avatar>
      <div class="profile-meta">
        <div class="profile-name">{{ user?.userName || '未设置用户名' }}</div>
        <a-tag color="blue">{{ user?.userRole || 'user' }}</a-tag>
      </div>
    </div>

    <div v-if="!isEditMode">
      <a-descriptions :column="1" bordered size="small">
        <a-descriptions-item label="用户 ID">
          {{ user?.id || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="账号">
          {{ user?.userAccount || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="用户名">
          {{ user?.userName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="头像链接">
          <span class="break-all">{{ user?.userAvatar || '-' }}</span>
        </a-descriptions-item>
        <a-descriptions-item label="个人简介">
          {{ user?.userProfile || '暂无简介' }}
        </a-descriptions-item>
        <a-descriptions-item label="创建时间">
          {{ formatTime(user?.createTime) || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="更新时间">
          {{ formatTime(user?.updateTime) || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </div>

    <a-form v-else layout="vertical">
      <a-form-item label="账号">
        <a-input :value="user?.userAccount" disabled />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input
          v-model:value="formData.userName"
          placeholder="请输入用户名"
          :maxlength="50"
          show-count
        />
      </a-form-item>
      <a-form-item label="头像链接">
        <a-input
          v-model:value="formData.userAvatar"
          placeholder="请输入头像图片链接"
        />
      </a-form-item>
      <a-form-item label="个人简介">
        <a-textarea
          v-model:value="formData.userProfile"
          placeholder="介绍一下自己"
          :rows="4"
          :maxlength="200"
          show-count
        />
      </a-form-item>
      <div v-if="formData.userAvatar" class="avatar-preview">
        <span class="avatar-preview-label">头像预览</span>
        <a-avatar :src="formData.userAvatar" :size="56">
          {{ (formData.userName || user?.userName || 'U').charAt(0) }}
        </a-avatar>
      </div>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { formatTime } from '@/utils/time'

interface Props {
  open: boolean
  user?: API.LoginUserVO
  submitting?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  submitting: false,
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit', value: API.UserUpdateRequest): void
}>()

const isEditMode = ref(false)

const formData = reactive<API.UserUpdateRequest>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

const resetForm = () => {
  formData.userName = props.user?.userName || ''
  formData.userAvatar = props.user?.userAvatar || ''
  formData.userProfile = props.user?.userProfile || ''
}

watch(
  () => [props.open, props.user],
  () => {
    resetForm()
    isEditMode.value = false
  },
  { immediate: true },
)

const displayName = computed(() => formData.userName || props.user?.userName || '')
const displayAvatar = computed(() => formData.userAvatar || props.user?.userAvatar || '')

const handleClose = () => {
  resetForm()
  isEditMode.value = false
  emit('update:open', false)
}

const startEdit = () => {
  resetForm()
  isEditMode.value = true
}

const cancelEdit = () => {
  resetForm()
  isEditMode.value = false
}

const handleSubmit = () => {
  emit('submit', {
    userName: formData.userName?.trim(),
    userAvatar: formData.userAvatar?.trim(),
    userProfile: formData.userProfile?.trim(),
  })
}
</script>

<style scoped>
.profile-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.profile-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profile-name {
  font-size: 18px;
  font-weight: 600;
  color: #1f1f1f;
}

.avatar-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
}

.avatar-preview-label {
  color: #666;
  font-size: 14px;
}

.break-all {
  word-break: break-all;
}
</style>
