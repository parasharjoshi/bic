/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

define(['ojs/ojcore', 'jquery'], function (oj, $) {
    var CommonUtility = {
        contentToBase64: function (content) {
            var data = "";
            var bytes = new Uint8Array(content);
            var length = bytes.byteLength;
            for (var i = 0; i < length; i++)
            {
                data += String.fromCharCode(bytes[i]);
            }
            //Encode BinaryString to base64
            data = window.btoa(data);
            return data;
        }

    };
    return CommonUtility;
});

