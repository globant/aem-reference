var AEM_REFSITE = AEM_REFSITE || {};

AEM_REFSITE.SmartImage4Croppings = CQ.Ext.extend(CQ.html5.form.SmartImage, {
    crops: {},

    constructor: function (config) {
        config = config || {};
        var tObj = this;
        var aRatios = {};

        $.each(config.json.imageCroppings, function (key, value) {

            var ratio = value.rect;
            var rect = '';
            var itemData = value.rect.split("/");

            if(itemData.length > 1){
                ratio = itemData[1];
                rect = value.rect;
            }

            aRatios[value.id] = {
                    "value": ratio,
                    "text": value.id
                };
            tObj.crops[value.id] = { text: value.id, cords : rect};
        });

        var defaults = { "cropConfig": { "aspectRatios": aRatios } };
        config = CQ.Util.applyDefaults(config, defaults);

        AEM_REFSITE.SmartImage4Croppings.superclass.constructor.call(this, config);
    },

    initComponent: function () {
        AEM_REFSITE.SmartImage4Croppings.superclass.initComponent.call(this);

        var imgTools = this.imageToolDefs;
        var cropTool;

        if(imgTools){
            for(var x = 0; x < imgTools.length; x++){
                if(imgTools[x].toolId == 'smartimageCrop'){
                    cropTool = imgTools[x];
                    break;
                }
            }
        }

        if(!cropTool){
            return;
        }

        var userInterface = cropTool.userInterface;

        this.on("loadimage", function(){
            var aRatios = userInterface.aspectRatioMenu.findByType("menucheckitem");

            // Selection of the first cropping ID available in the croppings menu
            // creation of the default cropping rectangle in case it is not available
            aRatios[0].checked = true;
            userInterface.toolbar.items.get("ratio").setText(
                    userInterface.aspectRatioText + ": " + aRatios[0].text);
            var initialValue = this.crops[aRatios[0].text].cords;


            if(initialValue == ''){

                var rect = "";
                var size = cropTool.workingArea.originalImageSize;

                if(size.height > size.width){
                    var ratios = aRatios[0].value.split(',');
                    var width = (parseInt(ratios[0]) * size.height) / parseInt(ratios[1]);
                    rect = "0,0," + width + "," + size.height;
                }else{
                    var ratios = aRatios[0].value.split(',');
                    var height = (parseInt(ratios[1]) * size.width) / parseInt(ratios[0]);
                    rect = "0,0,"  + size.width + "," + height;
                }

                initialValue = rect + '/' + aRatios[0].value;
            }
            cropTool.initialValue = initialValue;

            if(!aRatios){
                return;
            }

            for(var x = 0; x < aRatios.length; x++){
                aRatios[x].on('click', function(radio){
                    var key = this.getCropKey(radio.text);

                    if(!key){
                        return;
                    }

                    if(this.crops[key].cords){
                        this.setCoords(cropTool, this.crops[key].cords);
                    }else{
                        this.crops[key].cords = this.getRect(radio, userInterface);
                    }
                },this);

                var key = this.getCropKey(aRatios[x].text);
 
                if(key && this.dataRecord && this.dataRecord.data[key]){
                    this.crops[key].cords = this.dataRecord.data[key];
                }
            }
        });
 
        cropTool.workingArea.on("contentchange", function(changeDef){
            var aRatios = userInterface.aspectRatioMenu.findByType("menucheckitem");

            var aRatioChecked;
 
            if(aRatios){
                for(var x = 0; x < aRatios.length; x++){
                    if(aRatios[x].checked === true){
                        aRatioChecked = aRatios[x];
                        break;
                    }
                }
            }
 
            if(!aRatioChecked){
                return;
            }
 
            var key = this.getCropKey(aRatioChecked.text);
            this.crops[key].cords = this.getRect(aRatioChecked, userInterface);
        }, this);
    },
 
    getCropKey: function(text){
        for(var x in this.crops){
            if(this.crops.hasOwnProperty(x)){
                if(this.crops[x].text == text){
                    return x;
                }
            }
        }
 
        return null;
    },
 
    getRect: function (radio, ui) {
        var ratioStr = "";
        var aspectRatio = radio.value;
 
        if ((aspectRatio != null) && (aspectRatio != "0,0")) {
            ratioStr = "/" + aspectRatio;
        }

        if (ui.cropRect == null) {
            return ratioStr;
        }
 
        return ui.cropRect.x + "," + ui.cropRect.y + "," + (ui.cropRect.x + ui.cropRect.width) + ","
            + (ui.cropRect.y + ui.cropRect.height) + ratioStr;
    },
 
    setCoords: function (cropTool, cords) {
        cropTool.initialValue = cords;
        cropTool.onActivation();
    },

    onBeforeSubmit: function() {
        var requestparams = {};
        requestparams["path"] = this.path;

        $.each(this.crops, function (key, value) {
            requestparams[value.text] = value.cords;
        });

        CQ.Ext.Ajax.request({
            url: '/bin/services/core/public/setcroppings',
            method: 'GET',
            params: requestparams
        });

        this.reset();
        return true;
    }
});
 
CQ.Ext.reg("smartimage4croppings", AEM_REFSITE.SmartImage4Croppings);