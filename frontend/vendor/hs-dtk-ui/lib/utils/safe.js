'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
exports.Safe = undefined;

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }(); /**
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      * 安全处理
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      */


var _hex = require('./crypto/hex.js');

var _hex2 = _interopRequireDefault(_hex);

var _sm = require('./crypto/sm4-1.0');

var _sm2 = _interopRequireDefault(_sm);

var _sm3 = require('./crypto/sm3-1.0');

var _sm4 = _interopRequireDefault(_sm3);

var _sm5 = require('./crypto/sm2-1.0');

var _sm6 = _interopRequireDefault(_sm5);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

var Safe = exports.Safe = function () {
  function Safe() {
    _classCallCheck(this, Safe);
  }

  _createClass(Safe, null, [{
    key: 'sm2_encrypt',

    /**
    * 国密算法：SM2加密
    * @param {*} publicKey 加密公钥
    * @param {*} inputText 待加密文本
    * @param {*} cipherMode 加密模式 [0:C1C2C3;C1C3C2:1] 
    * @returns
    */
    value: function sm2_encrypt(publicKey) {
      var inputText = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';
      var cipherMode = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : 0;

      var smutil = new _sm6.default();
      return smutil.sm2encrypt(inputText, publicKey, cipherMode);
    }

    /**
    * 国密算法：SM3加密
    * @param {*} inputText  待加密文本
    * @returns
    */

  }, {
    key: 'sm3_encrypt',
    value: function sm3_encrypt() {
      var inputText = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';

      var dataBy = _hex2.default.utf8StrToBytes(inputText);
      var sm3 = new _sm4.default();
      sm3.update(dataBy, 0, dataBy.length); // 数据很多的话，可以分多次update
      var sm3Hash = sm3.doFinal(); // 得到的数据是个byte数组
      var sm3HashHex = _hex2.default.encode(sm3Hash, 0, sm3Hash.length); // 编码成16进制可见字符
      return sm3HashHex;
    }

    /**
     * 国密算法：SM4加密【ecb模式】
     * @param {*} keyText 密钥(16进制)
     * @param {*} inputText 待加密文本
     */

  }, {
    key: 'sm4_encrypt_ecb',
    value: function sm4_encrypt_ecb(keyText) {
      var inputText = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';

      var inputBytes = _hex2.default.utf8StrToBytes(inputText);
      var key = _hex2.default.decode(keyText);
      var sm4 = new _sm2.default();
      var cipher = sm4.encrypt_ecb(key, inputBytes);
      return _hex2.default.encode(cipher, 0, cipher.length);
    }

    /**
     * 国密算法：SM4解密【ecb模式】
     * @param {*} keyText 密钥(16进制)
     * @param {*} inputText 待解密文本
     */

  }, {
    key: 'sm4_decrypt_ecb',
    value: function sm4_decrypt_ecb(keyText) {
      var inputText = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : '';

      var inputBytes = _hex2.default.decode(inputText);
      var key = _hex2.default.decode(keyText);
      var sm4 = new _sm2.default();
      var cipher = sm4.decrypt_ecb(key, inputBytes);
      return _hex2.default.bytesToUtf8Str(cipher);
    }

    /**
     * 国密算法：SM4加密【cbc模式】
     * @param {*} keyText 密钥(16进制)
     * @param {*} ivText cbc模式IV(16进制)
     * @param {*} inputText 待加密文本
     */

  }, {
    key: 'sm4_encrypt_cbc',
    value: function sm4_encrypt_cbc(keyText, ivText) {
      var inputText = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : '';

      var inputBytes = _hex2.default.utf8StrToBytes(inputText);
      var key = _hex2.default.decode(keyText);
      var iv = _hex2.default.decode(ivText);
      var sm4 = new _sm2.default();
      var cipher = sm4.encrypt_cbc(key, iv, inputBytes);
      return _hex2.default.encode(cipher, 0, cipher.length);
    }

    /**
     * 国密算法：SM4解密【cbc模式】
     * @param {*} keyText 密钥(16进制)
     * @param {*} ivText cbc模式IV(16进制)
     * @param {*} inputText 待解密文本
     */

  }, {
    key: 'sm4_decrypt_cbc',
    value: function sm4_decrypt_cbc(keyText, ivText) {
      var inputText = arguments.length > 2 && arguments[2] !== undefined ? arguments[2] : '';

      var inputBytes = _hex2.default.decode(inputText);
      var key = _hex2.default.decode(keyText);
      var iv = _hex2.default.decode(ivText);
      var sm4 = new _sm2.default();
      var cipher = sm4.decrypt_cbc(key, iv, inputBytes);
      return _hex2.default.bytesToUtf8Str(cipher);
    }
  }]);

  return Safe;
}();

exports.default = Safe;