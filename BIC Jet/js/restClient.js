/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

define(['ojs/ojcore', 'jquery'], function (oj, $) {
    var RestClient = {
        invokePostWithCallback: function (url, data, successCallback, failureCallback) {
            return $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                'type': 'POST',
                'url': url,
                'data': JSON.stringify(data),
                'dataType': 'json',
                'success': successCallback,
                'error': failureCallback
            });
        },
        invokePost: function (url, data) {
            return $.ajax({
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                'type': 'POST',
                'url': url,
                'data': JSON.stringify(data),
                'dataType': 'json'
            });
        },
        invokeGet: function (url) {
            return $.ajax({
                headers: {
                    'Accept': 'application/json',
                },
                'type': 'GET',
                'url': url,
            });
        }
    };
    return RestClient;
});

