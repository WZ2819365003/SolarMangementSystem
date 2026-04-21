'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
var apirequestShim = function apirequestShim() {
  return new Promise(function (resolve) {
    console.warn('no `apirequest` found from host!');
    resolve({});
  });
};

var apirequest = exports.apirequest = window._tsx && window._tsx.apirequest || apirequestShim;