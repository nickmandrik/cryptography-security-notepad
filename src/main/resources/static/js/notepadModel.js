function NotepadModel() {

    var self = this;

    var rsaLengthKey = 2048;

    var rsaKey;

    // urls to request
    const urlGenerateKey = "/key/generate";
    const urlDownloadFile = "/download";
    const urlSendOpenRSAKey = "/upload/public-rsa-key";
    const urlGetSessionKey = "/key/get";
    const urlCheckPartsExist = "/check-parts-exists";

    self.sendRequestGenerateKey = function () {
        $.ajax({
            url: urlGenerateKey,
            type: 'GET',
            contentType: false,
            processData: false,
            success: function (data) {
                console.log("\nKey generated: " + data["result"] + '\n\n');
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus + ': ' + errorThrown);
            }
        });
    };


    self.sendRequestDownloadFile = function () {

        var data = {
            fileName: document.getElementById('fileName').value
        };
        $.ajax({
            data: JSON.stringify(data),
            url: urlDownloadFile,
            type: 'POST',
            contentType: "application/json",
            success: function (data) {
                console.log(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus + ': ' + errorThrown);
            }
        });
    };

    self.generateRSAKeys = function () {
        var passPhrase = "Password";
        var rsaKey = cryptico.generateRSAKey(passPhrase, rsaLengthKey);
        var mattsPublicKeyString = cryptico.publicKeyString(rsaKey);
        var key = JSON.stringify(rsaKey);
        self.rsaKey = rsaKey;
        localStorage.setItem("rsa_key", key);
        console.log("\nPublic RSA key:\n" + mattsPublicKeyString + "\n\n");
    };

    self.sendOpenRSAKey = function () {
        getRSAKeyFromStorage().then(function (key) {
            var data = {
                key: cryptico.publicKeyString(self.rsaKey)
            };
            $.ajax({
                data: JSON.stringify(data),
                url: urlSendOpenRSAKey,
                type: 'POST',
                contentType: "application/json",
                success: function (data) {
                    console.log('\nResult of sending RSA public key: ' + data["result"] + '\n\n');
                }
            });

        });
    };

    self.getSessionKeyEncryptRSA = function() {
        $.ajax({
            url: urlGetSessionKey,
            type: 'GET',
            contentType: "application/json",
            success: function (data) {
                console.log('\nResult of sending RSA public key: ' + data["result"] + '\n\n');
            }
        });
    }
}