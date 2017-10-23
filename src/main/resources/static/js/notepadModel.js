function NotepadModel() {

    var self = this;

    var sizeKeyRSA = 1024;

    // urls to request
    const urlGenerateKey = "/generate/key";
    const urlDownloadFile = "/download";
    const urlSendOpenRSAKey = "/upload/public-rsa-key";
    const urlCheckPartsExist = "/check-parts-exists";

    var privateRSAKey,
        publicRSAKey;

    self.sendRequestGenerateKey = function () {
        $.ajax({
            url: urlGenerateKey,
            type: 'GET',
            contentType: false,
            processData: false,
            success: function (data) {
                alert("Session key generated:\n" + data["sessionKey"]);
                console.log("Key generated:" + data["sessionKey"]);
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
        var dt = new Date();
        var time = -(dt.getTime());
        var pemPrivate;
        var pemPublic;


    };


    self.sendOpenRSAKey = function () {
        var data = {
            publicRSAKey: publicRSAKey
        };
        $.ajax({
            data: JSON.stringify(data),
            url: urlSendOpenRSAKey,
            type: 'POST',
            contentType: "application/json",
            success: function (data) {
                console.log(data);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                alert(textStatus + ': ' + errorThrown);
            }
        });
    }
}