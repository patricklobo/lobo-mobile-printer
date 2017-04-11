var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');

function MobilePrinter() {

}

MobilePrinter.prototype.getInfo = function(successCallback, errorCallback) {
    argscheck.checkArgs('fF', 'MobilePrinter.getInfo', arguments);
    exec(successCallback, errorCallback, "MobilePrinter", "getMobilePrinterInfo", []);
};

MobilePrinter.prototype.getUnicode = function(posts, successCallback, errorCallback) {
    // argscheck.checkArgs('fF', 'MobilePrinter.getUnicode', arguments);
    exec(successCallback, errorCallback, "MobilePrinter", "getUnicode", [{post:posts}]);
};
MobilePrinter.prototype.codigo = function(codigo, successCallback, errorCallback) {
    // argscheck.checkArgs('fF', 'MobilePrinter.getUnicode', arguments);
    exec(successCallback, errorCallback, "MobilePrinter", "codigo", [{codigo:codigo}]);
};

module.exports = new MobilePrinter();
