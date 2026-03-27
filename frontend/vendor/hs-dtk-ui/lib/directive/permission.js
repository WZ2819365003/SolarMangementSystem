'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
/**
 * 权限控制指令
 * 使用 Vue.use(permission, permissionData)
 */
exports.default = {
  install: function install(Vue, permissionData) {
    console.log('shenjp==>>ssss', permissionData);
    Vue.directive('permission', {
      inserted: function inserted(el, binding) {
        if (!binding.value || !Array.isArray(permissionData)) return;
        if (permissionData.length && !permissionData.includes(String(binding.value))) {
          el.disabled = true;
          el.classList.add('is-disabled');
        }
      },
      unbind: function unbind() {}
    });
  }
};