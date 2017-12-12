var isNew = false;

var rsa_opts = {
    name: "RSA-OAEP",
    modulusLength: 1024,
    publicExponent: new Uint8Array([1, 0, 1]),  // 24 bit representation of 65537
    hash: {name: "SHA-1"}
};

function getRSAKeyFromStorage() {
    var key = localStorage.getItem("rsa_key");
    return Promise.resolve(JSON.parse(key));
}

function convertPublicKey(key) {
    return crypto.subtle.importKey("jwk", key, rsa_opts, true, ["encrypt"])
        .then(function (result) {
            return result;
        });
}

//Utility function
function str2ab(str) {
    var arrBuff = new ArrayBuffer(str.length);
    var bytes = new Uint8Array(arrBuff);
    for (var iii = 0; iii < str.length; iii++) {
        bytes[iii] = str.charCodeAt(iii);
    }
    return bytes;
}

function ab2str(buf) {
    return String.fromCharCode.apply(null, new Uint16Array(buf));
}

function getRSAKey() {
    isNew = true;

    return createAndSaveAKeyPair()
        .then(function (data) {
            return Promise.all([crypto.subtle.exportKey("jwk", data.privateKey), crypto.subtle.exportKey("jwk", data.publicKey)]);
        })
        .then(function (result) {
            var key = JSON.stringify({public: result[1], private: result[0]});
            localStorage.setItem("rsa_key", key);
            return Promise.resolve(JSON.parse(key));
        })
        .catch(function (e) {
            console.warn(e);
        })
}

var keyPair;

function createAndSaveAKeyPair() {
    return window.crypto.subtle.generateKey(
        rsa_opts,
        true,   // can extract it later if we want
        ["encrypt", "decrypt"])
        .then(function (key) {
            keyPair = key;
            return key;
        });
}


function generateAndLogRsaKey() {
    var dt = new Date();
    var time = -(dt.getTime());
    getRSAKey().then( function(key) {
        time += (dt.getTime());
        console.log("\nRSA key generated in " + time + "ms.\n\nRSA public key:\n" +
            key.public["n"].length + "\n\nRSA private key:\n" + key.private["d"].length + "\n\n");
    })
}

function base64UrlDecode(str) {
    str = atob(str.replace(/-/g, '+').replace(/_/g, '/'));
    var buffer = new Uint8Array(str.length);
    for(var i = 0; i < str.length; ++i) {
        buffer[i] = str.charCodeAt(i);
    }
    return buffer;
}