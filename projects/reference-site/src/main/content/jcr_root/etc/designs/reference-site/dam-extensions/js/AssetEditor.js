var AEM_REFSITE = AEM_REFSITE || {};

AEM_REFSITE.AssetEditor = CQ.Ext.extend(CQ.dam.AssetEditor, {
    /**
     * Constructor
     */
    constructor : function(config) {
        var imagepath = config.path;
        var json = this.getCroppingsIds(config.path);
        var buttons = {};

        if(json.imageCroppings.length > 0){
             buttons.bbarWest = [
                    CQ.dam.AssetEditor.REFRESH_INFO,
                    "->",
                    {
                        "text": CQ.I18n.getMessage("Image Croppings..."),
                        "disabled": this.readOnly,
                        "cls": "cq-btn-edit",
                        "scope": this,
                        "minWidth": CQ.dam.themes.AssetEditor.MIN_BUTTON_WIDTH,
                        "handler": function() {
                            var config = CQ.WCM.getDialogConfig({
                                "name": "./original",
                                "xtype": "smartimage4croppings",
                                "cropParameter": "crop",
                                "rotateParameter": "0",
                                "disableFlush": true,
                                "disableInfo": true,
                                "json": this.getCroppingsIds(imagepath),
                                "path": imagepath
                            });

                            var ae = this;
                            config = CQ.Util.applyDefaults(config, {
                                "title": CQ.I18n.getMessage("Image Croppings Editor"),
                                "y": 50,
                                "width": 800,
                                "height": 600,
                                "formUrl": this.pathEncoded + ".assetimage.html",
                                "responseScope": this
                            });
                            var dialog = CQ.Util.build(config, true);
                            dialog.loadContent(this.pathEncoded + "/jcr:content/renditions");
                            dialog.show();
                        }
                    },
                    CQ.dam.AssetEditor.EDIT_IMAGE
                ];
        }

        config = CQ.Util.applyDefaults(config, buttons);
        AEM_REFSITE.AssetEditor.superclass.constructor.call(this, config);
    },

    getCroppingsIds: function(path){
      var request = ((window.XMLHttpRequest) ? new XMLHttpRequest() : new ActiveXObject("Microsoft.XMLHTTP"));
      request.open("GET", '/bin/services/core/public/allowedcroppings?path=' + path, false);
      request.send(null);
      return CQ.Ext.decode(request.responseText);
    }
});

CQ.Ext.reg("ref_site_asseteditor", AEM_REFSITE.AssetEditor);