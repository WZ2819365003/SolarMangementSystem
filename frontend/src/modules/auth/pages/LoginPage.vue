<template>
  <div class="pv-login">
    <el-form
      ref="form"
      class="pv-login__card"
      :model="form"
      :rules="rules"
      label-position="top"
      size="medium"
      @submit.native.prevent="handleSubmit"
    >
      <div class="pv-login__brand">
        <div class="pv-login__mark">
          PV
        </div>
        <h1 class="pv-login__title">
          {{ appName }}
        </h1>
      </div>

      <el-form-item label="用户名" prop="username">
        <el-input v-model.trim="form.username" autocomplete="username" autofocus />
      </el-form-item>
      <el-form-item label="密码" prop="password">
        <el-input
          v-model="form.password"
          type="password"
          autocomplete="current-password"
          show-password
        />
      </el-form-item>

      <div v-if="errorMessage" class="pv-login__error" role="alert">
        {{ errorMessage }}
      </div>

      <el-button
        class="pv-login__submit"
        type="primary"
        native-type="submit"
        :loading="submitting"
      >
        登录
      </el-button>
    </el-form>
  </div>
</template>

<script>
import { getRequestErrorMessage, login } from '@/shared/host/bridge'

export default {
  name: 'LoginPage',
  data() {
    return {
      form: { username: '', password: '' },
      rules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
      },
      submitting: false,
      errorMessage: ''
    }
  },
  computed: {
    appName() {
      return this.$store.state.appName
    }
  },
  methods: {
    // 只接受站内路径，防止 ?redirect= 被用来跳到外部地址
    redirectTarget() {
      const { redirect } = this.$route.query
      return typeof redirect === 'string' && /^\/(?![/\\])/.test(redirect)
        ? redirect
        : '/'
    },
    async handleSubmit() {
      if (this.submitting) {
        return
      }
      const valid = await this.$refs.form.validate().catch(() => false)
      if (!valid) {
        return
      }
      this.submitting = true
      this.errorMessage = ''
      try {
        await login(this.form.username, this.form.password)
        this.$store.dispatch('initApp', {})
        this.$router.replace(this.redirectTarget())
      } catch (error) {
        this.form.password = ''
        this.errorMessage = getRequestErrorMessage(error, '登录失败，请稍后重试')
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="less" scoped>
.pv-login {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 16px;
  background:
    radial-gradient(circle at top right, rgba(26, 141, 255, 0.16), transparent 30%),
    linear-gradient(180deg, rgba(7, 27, 61, 0.98), rgba(5, 18, 45, 1));
}

.pv-login__card {
  width: 100%;
  max-width: 380px;
  padding: 32px;
  border: 1px solid rgba(255, 255, 255, 0.08);
  border-radius: 4px;
  background: rgba(7, 31, 74, 0.96);
}

.pv-login__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;
}

.pv-login__mark {
  display: flex;
  flex: none;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  border-radius: 4px;
  background: linear-gradient(135deg, rgba(6, 162, 153, 0.82), rgba(26, 141, 255, 0.76));
  color: #fff;
  font-weight: 700;
}

.pv-login__title {
  margin: 0;
  color: #fff;
  font-size: 20px;
  font-weight: 600;
}

.pv-login__error {
  margin-bottom: 16px;
  color: #ff8f8f;
  font-size: 13px;
}

.pv-login__submit {
  width: 100%;
}

.pv-login /deep/ .el-form-item__label {
  color: rgba(255, 255, 255, 0.72);
}

.pv-login /deep/ .el-input__inner {
  border-color: rgba(255, 255, 255, 0.16);
  background: rgba(255, 255, 255, 0.05);
  color: #fff;
}
</style>
