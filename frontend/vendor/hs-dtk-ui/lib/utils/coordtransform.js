'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _extends = Object.assign || function (target) { for (var i = 1; i < arguments.length; i++) { var source = arguments[i]; for (var key in source) { if (Object.prototype.hasOwnProperty.call(source, key)) { target[key] = source[key]; } } } return target; };

var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _toArray(arr) { return Array.isArray(arr) ? arr : Array.from(arr); }

/**
 * @description 坐标系转换 参考： https://blog.csdn.net/xialong_927/article/details/100208002
 * @file coordTransform.js
 * @author shenjieping@techstar.com.cn
 * @data 2022-07-29 20:19:59
 */

var PI = 3.1415926535897932384626;
var ee = 0.00669342162296594323;
var a = 6378245.0;
var x_PI = PI * 3000.0 / 180.0;

var coordTransform = {
  /**
   * 高德坐标转wgs84
   * @param {Array} lngLat 经纬度
   * @returns wgs84坐标
   */
  gcj02towgs84: function gcj02towgs84(lngLat) {
    var _lngLat = _slicedToArray(lngLat, 2),
        lng = _lngLat[0],
        lat = _lngLat[1];

    lng = +lng;
    lat = +lat;
    if (this.out_of_china(lng, lat)) {
      return [lng, lat];
    } else {
      var dlat = this.transformlat(lng - 105.0, lat - 35.0);
      var dlng = this.transformlng(lng - 105.0, lat - 35.0);
      var radlat = lat / 180.0 * PI;
      var magic = Math.sin(radlat);
      magic = 1 - ee * magic * magic;
      var sqrtmagic = Math.sqrt(magic);
      dlat = dlat * 180.0 / (a * (1 - ee) / (magic * sqrtmagic) * PI);
      dlng = dlng * 180.0 / (a / sqrtmagic * Math.cos(radlat) * PI);
      var mglat = lat + dlat;
      var mglng = lng + dlng;
      var res = [lng * 2 - mglng, lat * 2 - mglat];
      return res.map(function (i) {
        return i.toFixed(6) * 1;
      });
    }
  },

  /**
   * wgs84转高德坐标
   * @param {Array} lngLat 经纬度
   * @returns 高德坐标
   */
  wgs84togcj02: function wgs84togcj02(lngLat) {
    var _lngLat2 = _slicedToArray(lngLat, 2),
        lng = _lngLat2[0],
        lat = _lngLat2[1];

    lng = +lng;
    lat = +lat;
    if (this.out_of_china(lng, lat)) {
      return [lng, lat];
    } else {
      var dlat = this.transformlat(lng - 105.0, lat - 35.0);
      var dlng = this.transformlng(lng - 105.0, lat - 35.0);
      var radlat = lat / 180.0 * PI;
      var magic = Math.sin(radlat);
      magic = 1 - ee * magic * magic;
      var sqrtmagic = Math.sqrt(magic);
      dlat = dlat * 180.0 / (a * (1 - ee) / (magic * sqrtmagic) * PI);
      dlng = dlng * 180.0 / (a / sqrtmagic * Math.cos(radlat) * PI);
      var mglat = lat + dlat;
      var mglng = lng + dlng;
      return [mglng, mglat].map(function (i) {
        return i.toFixed(6) * 1;
      });
    }
  },

  /**
   * 百度坐标系 (BD-09) 与 火星坐标系 (GCJ-02)的转换
   * 即 百度 转 谷歌、高德
   * @param {Array} lnglat 经纬度
   * @returns 高德坐标
   */
  bd09togcj02: function bd09togcj02(lnglat) {
    var lng = +lnglat[0];
    var lat = +lnglat[1];
    var x = lng - 0.0065;
    var y = lat - 0.006;
    var z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_PI);
    var theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_PI);
    var gg_lng = z * Math.cos(theta);
    var gg_lat = z * Math.sin(theta);
    return [gg_lng, gg_lat].map(function (i) {
      return i.toFixed(6) * 1;
    });
  },

  /**
   * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换
   * 即谷歌、高德 转 百度
   * @param {Array} lnglat 经纬度
   * @returns 百度坐标
   */
  gcj02tobd09: function gcj02tobd09(lnglat) {
    var lat = +lnglat[0];
    var lng = +lnglat[1];
    var z = Math.sqrt(lng * lng + lat * lat) + 0.00002 * Math.sin(lat * x_PI);
    var theta = Math.atan2(lat, lng) + 0.000003 * Math.cos(lng * x_PI);
    var bd_lng = z * Math.cos(theta) + 0.0065;
    var bd_lat = z * Math.sin(theta) + 0.006;
    return [bd_lng, bd_lat].map(function (i) {
      return i.toFixed(6) * 1;
    });
  },
  out_of_china: function out_of_china(lng, lat) {
    lat = +lat;
    lng = +lng;
    // 纬度3.86~53.55,经度73.66~135.05 
    return !(lng > 73.66 && lng < 135.05 && lat > 3.86 && lat < 53.55);
  },
  transformlat: function transformlat(lng, lat) {
    // const PI = PI
    lat = +lat;
    lng = +lng;
    var ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lat * PI) + 40.0 * Math.sin(lat / 3.0 * PI)) * 2.0 / 3.0;
    ret += (160.0 * Math.sin(lat / 12.0 * PI) + 320 * Math.sin(lat * PI / 30.0)) * 2.0 / 3.0;
    return ret;
  },
  transformlng: function transformlng(lng, lat) {
    lat = +lat;
    lng = +lng;
    var ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
    ret += (20.0 * Math.sin(6.0 * lng * PI) + 20.0 * Math.sin(2.0 * lng * PI)) * 2.0 / 3.0;
    ret += (20.0 * Math.sin(lng * PI) + 40.0 * Math.sin(lng / 3.0 * PI)) * 2.0 / 3.0;
    ret += (150.0 * Math.sin(lng / 12.0 * PI) + 300.0 * Math.sin(lng / 30.0 * PI)) * 2.0 / 3.0;
    return ret;
  }
};

/**
 * @description geojson坐标系转换
 * @param {geojson} geojson 数据
 * @param {string} fnName 转换函数名称
 * @returns 转换之后的geojson
 */
var transformGeojson = function transformGeojson(geojson, fnName) {
  var features = geojson.features;

  if (!fnName) fnName = 'wgs84togcj02';
  geojson.features = features.map(function (feat) {
    var geometry = feat.geometry;
    var coordinates = geometry.coordinates,
        type = geometry.type;

    if (type === 'Polygon') {
      coordinates = coordinates.map(function (coor) {
        coor = coor.map(function (point) {
          var _point = _toArray(point),
              lng = _point[0],
              lat = _point[1],
              args = _point.slice(2);

          var trans = coordTransform[fnName]([lng, lat]);
          return [].concat(_toConsumableArray(trans), _toConsumableArray(args));
        });
        return coor;
      });
    } else if (type === 'Point') {
      var _coordinates = coordinates,
          _coordinates2 = _toArray(_coordinates),
          lng = _coordinates2[0],
          lat = _coordinates2[1],
          args = _coordinates2.slice(2);

      var trans = coordTransform[fnName]([lng, lat]);
      coordinates = [].concat(_toConsumableArray(trans), _toConsumableArray(args));
    } else if (type === 'MultiPolygon' || type === 'MultiLineString') {
      coordinates = coordinates.map(function (coor) {
        coor = coor.map(function (box) {
          box = box.map(function (point) {
            var _point2 = _toArray(point),
                lng = _point2[0],
                lat = _point2[1],
                args = _point2.slice(2);

            var trans = coordTransform[fnName]([lng, lat]);
            return [].concat(_toConsumableArray(trans), _toConsumableArray(args));
          });
          return box;
        });
        return coor;
      });
    } else if (type === 'LineString' || type === 'MultiPoint') {
      coordinates = coordinates.map(function (coor) {
        var _coor = _toArray(coor),
            lng = _coor[0],
            lat = _coor[1],
            args = _coor.slice(2);

        var trans = coordTransform[fnName]([lng, lat]);
        return [].concat(_toConsumableArray(trans), _toConsumableArray(args));
      });
    }
    return _extends({}, feat, {
      geometry: _extends({}, geometry, {
        coordinates: coordinates
      })
    });
  });
  return geojson;
};

exports.coordTransform = coordTransform;
exports.transformGeojson = transformGeojson;