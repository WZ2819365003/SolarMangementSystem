import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import HsDtkUi from 'hs-dtk-ui'
import 'hs-dtk-ui/lib/theme-chalk/index.css'

Vue.use(ElementUI, {
  size: 'small'
})

if (HsDtkUi && HsDtkUi.install) {
  Vue.use(HsDtkUi)
}
