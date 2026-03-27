'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Base = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }(); /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      * 基础公共方法
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      */


var _lodash = require('lodash');

var _vueClipboards = require('vue-clipboards');

var _vueClipboards2 = _interopRequireDefault(_vueClipboards);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Base = exports.Base = function () {
  function Base() {
    _classCallCheck(this, Base);
  }

  _createClass(Base, null, [{
    key: 'guid',

    // constructor() {
    // }
    /**
     * 生成GUID
     * @returns
     */
    value: function guid() {
      var d = new Date().getTime();
      return 'xxxxxxxxxxxxxxxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c === 'x' ? r : r & 0x3 | 0x8).toString(16);
      });
    }

    /**
     * 获取URL参数
     * @param {*} name
     * @returns
     */

  }, {
    key: 'urlParamValue',
    value: function urlParamValue(name) {
      var reg = new RegExp('(^|&)' + name + '=([^&]*)(&|$)');
      var r = location.href.substr(location.href.indexOf('?') + 1).match(reg);
      if (r != null) {
        var param = unescape(r[2]);
        if (param.indexOf('#') != -1) return param.substr(0, param.indexOf('#'));else return param;
      }
      return null;
    }

    /**
     * 函数防抖
     * @param {Function} fn 要防抖动的函数
     * @param {Number} wait 延迟时间
     * @param {Object} options 选项
     * @returns
     */

  }, {
    key: 'debounce',
    value: function debounce(fn) {
      var wait = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;
      var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

      if (typeof fn !== 'function') {
        throw Error('Please input function!');
      }
      return (0, _lodash.debounce)(fn, wait, options);
    }

    /**
     *
     * @param {Function} fn 要节流的函数
     * @param {Number} wait 延迟时间
     * @param {Object} options 选项
     * @returns
     */

  }, {
    key: 'throttle',
    value: function throttle(fn) {
      var wait = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;
      var options = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : {};

      if (typeof fn !== 'function') {
        throw Error('Please input function!');
      }
      return (0, _lodash.throttle)(fn, wait, options);
    }

    /**
     * 复制到剪切板
     * @returns VueClipboards 通过 Vue.use调用。clipboard 在组件内通过directives 调用
     */

  }, {
    key: 'clipboards',
    value: function clipboards() {
      return {
        VueClipboards: _vueClipboards2.default,
        clipboard: _vueClipboards.clipboard
      };
    }
  }]);

  return Base;
}();

exports.default = Base;