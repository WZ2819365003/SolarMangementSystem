'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Data = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }(); /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      * 数据处理
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      */


var _lodash = require('lodash');

var _dayjs = require('dayjs');

var _dayjs2 = _interopRequireDefault(_dayjs);

var _coordtransform = require('./coordtransform');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Data = exports.Data = function () {
  function Data() {
    _classCallCheck(this, Data);
  }

  _createClass(Data, null, [{
    key: 'clone',

    /**
     * 深拷贝
     * @param {Any} value 需要克隆的值
     * @returns 新值
     */
    value: function clone(value) {
      return (0, _lodash.cloneDeep)(value);
    }

    /**
     * 操作cookie
     * @returns getCookie 获取cookie, setCookie 设置cookie removeCookie 删除cookie
     */

  }, {
    key: 'cookie',
    value: function cookie() {
      return {
        getCookie: function getCookie(name) {
          var arr = void 0;
          var reg = new RegExp("(^| )" + name + "=([^;]*)(;|$)");
          if (arr = document.cookie.match(reg)) {
            return unescape(arr[2]);
          }
          return null;
        },
        setCookie: function setCookie(name, value) {
          var date = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 0;

          var strsec = 0;
          if (typeof date === 'number') {
            strsec = date;
          } else {
            var num = date.substring(0, date.length - 1) * 1;
            var unit = date.substring(date.length - 1);
            if (unit === 's') {
              strsec = num * 1000;
            } else if (unit === 'm') {
              strsec = num * 60 * 1000;
            } else if (unit === 'h') {
              strsec = num * 60 * 60 * 1000;
            } else if (unit === 'd') {
              strsec = num * 24 * 60 * 60 * 1000;
            } else if (unit === 'y') {
              strsec = num * 365 * 24 * 60 * 60 * 1000;
            }
          }
          var exp = new Date();
          exp.setTime(exp.getTime() + strsec);
          document.cookie = name + "=" + escape(value) + ";expires=" + exp.toGMTString();
        },
        removeCookie: function removeCookie(name) {
          var exp = new Date();
          exp.setTime(exp.getTime() - 1);
          var cval = this.getCookie(name);
          if (cval != null) document.cookie = name + "=" + cval + ";expires=" + exp.toGMTString();
        }
      };
    }

    /**
     * 本地存储
     * @returns
     */

  }, {
    key: 'storage',
    value: function storage() {
      var storage = window.localStorage;
      return {
        setItem: function setItem(name, value) {
          storage.setItem(name, value);
        },
        getItem: function getItem(name) {
          return storage.getItem(name);
        },
        removeItem: function removeItem(name) {
          storage.removeItem(name);
        }
      };
    }

    /**
     * 格式化时间
     * @param {Any} time 格式化的时间
     * @param {*} format 格式化占位符 https://dayjs.gitee.io/docs/zh-CN/display/format
     * @returns 格式化之后的结果
     */

  }, {
    key: 'formatDate',
    value: function formatDate(time, format) {
      return (0, _dayjs2.default)(time).format(format);
    }

    /**
     * 坐标系转换，提供geojson坐标系转换
     * @returns 
     */

  }, {
    key: 'transformLnglat',
    value: function transformLnglat() {
      return {
        coordTransform: _coordtransform.coordTransform,
        transformGeojson: _coordtransform.transformGeojson
      };
    }

    /**
     * 格式化数字
     * @param {*} value 需要格式化的数值
     * @param {*} format 格式化占位符
     * # 不保留小数 #.## 保留2位小数 #% 百分比 #,### 千分位
     */

  }, {
    key: 'numberFormat',
    value: function numberFormat(value, format) {
      if (!/^-{0,1}[0-9]+\.{0,1}[0-9]{0,}$/g.test(value)) {
        throw Error('Place input Number!');
      }
      if (format === '#') {
        return parseInt(value);
      }
      if (/^#\.#{1,}$/.test(format)) {
        var digit = format.split('.')[1].length;
        return value.toFixed(digit);
      }
      if (/^#\.{0,1}#{0,}%$/.test(format)) {
        var _digit = format.split('.')[1];
        var num = _digit ? _digit.length - 1 : 0;
        return (value * 100).toFixed(num) + '%';
      }
      if (format === '#,###') {
        return String(value).replace(/\d{1,3}(?=(\d{3})+$)/g, function (s) {
          return s + ',';
        });
      }
      return value;
    }
  }]);

  return Data;
}();

exports.default = Data;