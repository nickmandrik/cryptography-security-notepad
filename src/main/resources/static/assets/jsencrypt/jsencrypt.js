function arrayBufferToBase64(arrayBuffer) {
    var byteArray = new Uint8Array(arrayBuffer);
    var byteString = '';
    for(var i=0; i < byteArray.byteLength; i++) {
        byteString += String.fromCharCode(byteArray[i]);
    }
    return window.btoa(byteString);
}

function toPem(privateKey) {
    return arrayBufferToBase64(privateKey);
}


function generateRSA() {
    window.crypto.subtle.generateKey(
        {
            name: "RSA-OAEP",
            modulusLength: 2048,
            publicExponent: new Uint8Array([0x01, 0x00, 0x01]),
            hash: {name: "SHA-256"}
        },
        true,
        ["encrypt", "decrypt"] //can be any combination of "sign" and "verify"
    ).then(function(keyPair){
        window.crypto.subtle.exportKey(
            "pkcs8",
            keyPair.privateKey
        ).then(function(exportedPrivateKey) {
            pemPrivate = toPem(exportedPrivateKey);
        }).catch(function(err) {
            console.log("Error to generate Private key: " + err);
        });

        window.crypto.subtle.exportKey(
            "spki",
            keyPair.publicKey
        ).then(function(exportedPublicKey) {
            pemPublic = toPem(exportedPublicKey);
            console.log(pemPublic)
        }).catch(function(err) {
            console.log("Error to generate Public key: " + err);
        });
    }).then(function() {
            dt = new Date();
            time += (dt.getTime());
            console.log("\nRSA key generated in " + time + "ms.\n\nRSA public key:\n" +
                toPem(pemPublic) + "\n\nRSA private key:\n" + toPem(pemPrivate) + "\n\n");
        }
    ).catch(function(err){
        console.error(err);
    });
}