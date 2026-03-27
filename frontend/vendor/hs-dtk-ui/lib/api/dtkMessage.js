'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _apirequest = require('../utils/apirequest');

var MSG = '/message/msg';
var TEMPLATE = '/message/template';
var APPLICATION = '/aif/application';
var CHANNEL = '/message/channel';
var GROUP = '/message/msg/group';
var OUTSIDER = '/message/group/outsider';

exports.default = {
  // 发送消息
  msgSend: function msgSend(data) {
    return (0, _apirequest.apirequest)({
      url: MSG + '/send',
      method: 'POST',
      data: data
    });
  },
  templateInfo: function templateInfo(id) {
    return (0, _apirequest.apirequest)({
      url: TEMPLATE + '/get',
      method: 'POST',
      data: {
        id: id
      }
    });
  },
  templateAllPage: function templateAllPage(data) {
    return (0, _apirequest.apirequest)({
      url: TEMPLATE + '/query_list',
      method: 'GET',
      data: data
    });
  },
  groupOptions: function groupOptions() {
    return (0, _apirequest.apirequest)({
      url: GROUP + '/query_list',
      method: 'GET'
    });
  },
  outsiderOptions: function outsiderOptions() {
    return (0, _apirequest.apirequest)({
      url: OUTSIDER + '/query_list',
      method: 'GET'
    });
  },
  appList: function appList() {
    return (0, _apirequest.apirequest)({
      url: APPLICATION + '/query_list',
      method: 'GET'
    });
  },
  channelList: function channelList() {
    return (0, _apirequest.apirequest)({
      url: CHANNEL + '/all_list',
      method: 'POST'
    });
  }
};