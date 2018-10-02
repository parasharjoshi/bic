/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */

define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojswitcher', 'ojs/ojprogress', 'ojs/ojtable',
    'ojs/ojarraydataprovider', 'ojs/ojknockout', 'ojs/ojfilepicker', 'ojs/ojradioset', 'ojs/ojinputtext', 'ojs/ojlabel',
    'ojs/ojinputnumber', 'ojs/ojselectcombobox', 'ojs/ojpopup'],
        function (oj, ko, $, app, client) {

            function settingsViewModel() {
                var self = this;
                self.isModuleEnabled = ko.observable();
                self.isAdmin = ko.observable(app.isAdmin());
                self.selectedTab = ko.observable("current-model");
                self.model = ko.observable();
                self.initializing = ko.observable();
                self.inprogress = ko.observable(undefined);
                self.progressValue = ko.observable();
                self.errorMessage = ko.observable();
                self.modelInfoTableDataProvider = ko.observable();
                self.disableRestoreButton = ko.observable();

                self.connected = function () {
                    self.resetFlags();
                    self.clearFiles(true);
                    if (self.isAdmin()) {
                        self.initializeModule();
                    } else {
                        self.errorMessage("This page is restricted to Admins only! Please login  with proper credentials!!!");
                        self.isModuleEnabled(true);
                    }
                };

                self.disconnected = function () {
                    // Implement if needed
                    self.modelSelectedForRestore(undefined);
                };

                self.transitionCompleted = function () {
                    // Implement if needed
                };

                self.tabSelectionChange = function (event) {
                    console.log("Tab selection changed" + event.detail.value);
                    self.resetFlags();
                    self.initializeModule();
                    self.configType("");
                    if (event.detail.value === "model-history") {
                        self.fetchAllModelInfo();
                    }
                };

                self.restoreModelConfirm = function () {
                    var popup = document.querySelector('#confirm-restore');
                    popup.open('#restore-model-button');
                };

                self.restoreModel = function () {
                    self.disableRestoreButton(true);
                    console.log("Restoring model" + self.modelSelectedForRestore());

                    client.invokePost(app.hostBasePath() + '/admin/restoreModel',
                            {userName: app.userLogin(), modelId: self.modelSelectedForRestore()})
                            .done(function (data) {
                                self.modelSelectedForRestore("");
                                if (data.error && data.error.errorCode) {
                                    self.errorMessage("Reatore failed. Please contact admin if problem persists. Reason: " + data.error.errorMessage);
                                } else {
                                    self.inprogress('DONE');
                                    self.initializeModule();
                                    self.selectedTab('current-model');
                                }
                                var popup = document.querySelector('#confirm-restore');
                                popup.close();
                                self.disableRestoreButton(false);
                            })
                            .fail(function (xhr, status, error) {
                                document.getElementById('auto-init-button').disabled = false;
                                self.inprogress('FAIL');
                                self.errorMessage("Operation failed. Please contact admin if problem persists.");
                                var popup = document.querySelector('#confirm-restore');
                                popup.close();
                                self.disableRestoreButton(false);
                            });

                };

                self.verifyTrainOptions = function () {
                    client.invokeGet(app.hostBasePath() + '/admin/isTraining')
                            .done(function (data) {
                                if (data.isTraining) {
                                    self.showTrainOptions(true);
                                } else {
                                    self.showTrainOptions(false);
                                }
                            })
                            .fail(function (xhr, status, error) {
                            });

                }

                self.resetFlags = function () {
                    self.uniqueTokenForUpload = undefined;
                    self.modelInfoTableDataProvider(undefined);
                    self.isModuleEnabled(undefined);
                    self.isAdmin(app.isAdmin());
                    self.model(undefined);
                    self.initializing(undefined);
                    self.inprogress(undefined);
                    self.progressValue(undefined);
                    self.errorMessage(undefined);
                    self.showTrainOptions(undefined);
                    self.modelSelectedForRestore(undefined);
                };

                self.initializeModule = function () {
                    self.progressValue(-1);
                    self.initializing(true);
                    client.invokeGet(app.hostBasePath() + '/admin/modelinfo')
                            .done(function (data) {
                                console.log("done...");
                                if (data.id) {
                                    self.model(data);
                                    self.populateModelData();

                                } else {
                                    self.errorMessage("There is no model available at the moment. Please contact admin.");
                                }
                                self.initializing(false);
                                self.isModuleEnabled(true);
                            })
                            .fail(function (xhr, status, error) {
                                console.log("fail...");
                                self.errorMessage("Initialation failed. Please contact admin if problem persists.");
                                self.initializing(false);
                                self.isModuleEnabled(false);
                            });
                    self.verifyTrainOptions();
                };

                self.autoInitModel = function () {
                    self.inprogress('YES');
                    document.getElementById('auto-init-button').disabled = true;
                    client.invokePost(app.hostBasePath() + '/downloadInitModel',
                            {userName: app.userLogin()})
                            .done(function (data) {
                                self.initializeModule();
                                self.inprogress('DONE');
                            })
                            .fail(function (xhr, status, error) {
                                document.getElementById('auto-init-button').disabled = false;
                                self.inprogress('FAIL');
                                self.errorMessage("Operation failed. Please contact admin if problem persists.");
                            });
                };

                self.modelHistoryColumnsArray = [{"headerText": "Id",
                        "field": "id"},
                    {"headerText": "Author",
                        "field": "userName"},
                    {"headerText": "Created",
                        "field": "created"},
                    {"headerText": "Description",
                        "field": "info"}];

                self.tableSelectionChanged = function (event) {
                    if (event.detail.value[0] && event.detail.value[0].startKey && self.model().id != event.detail.value[0].startKey.row) {
                        self.modelSelectedForRestore(event.detail.value[0].startKey.row);
                    } else {
                        self.modelSelectedForRestore(undefined);
                    }
                };

                self.fetchAllModelInfo = function () {
                    self.inprogress('YES');
                    client.invokeGet(app.hostBasePath() + '/admin/allmodel')
                            .done(function (data) {
                                console.log("Retrieved model info. Rows returned : " + data.length);
                                var historyArray = [];
                                if (data && data.length > 0) {
                                    data.forEach(
                                            function (item, i, array) {
                                                historyArray.push(
                                                        {
                                                            id: data[i].id,
                                                            info: data[i].info,
                                                            userName: data[i].userName,
                                                            created: new Date(data[i].createdDate),
                                                            modified: new Date(data[i].modificationDate),
                                                            auto: data[i].autoDownloaded
                                                        });
                                            });

                                } else {
                                    self.errorMessage("There is no model history available at the moment.");
                                }
                                self.modelDataProvider(new oj.ArrayDataProvider(historyArray, {keyAttributes: 'id'}));
                                self.inprogress('DONE');
                            })
                            .fail(function (xhr, status, error) {
                                self.modelDataProvider(new oj.ArrayDataProvider([], {keyAttributes: 'id'}));
                                self.inprogress('FAIL');
                                self.errorMessage("Operation failed. Please contact admin if problem persists.");
                            });
                };

                self.populateModelData = function () {
                    //Populate Model data
                    var modelProviderDataArray = [
                        {modelAttr: 'Model id/version', modelValue: self.model().id},
                        {modelAttr: 'Uploaded', modelValue: new Date(self.model().createdDate)},
                        {modelAttr: 'Modified', modelValue: new Date(self.model().modificationDate)},
                        {modelAttr: 'Upload by', modelValue: self.model().userName},
                        {modelAttr: 'Model file name', modelValue: self.model().modelFileName},
                        {modelAttr: 'Label file name', modelValue: self.model().labelFileName},
                        {modelAttr: 'Inception Model', modelValue: self.model().autoDownloaded},
                        {modelAttr: 'Description', modelValue: self.model().info}
                    ];
                    for (var i in self.model().attributes) {
                        modelProviderDataArray.push({modelAttr: i, modelValue: self.model().attributes[i]});
                    }

                    self.modelInfoTableDataProvider(new oj.ArrayDataProvider(modelProviderDataArray));
                };

                self.fileName = ko.observable();
                self.fileList = ko.observableArray([]);
                self.file = ko.observable();
                self.acceptStr = ko.observable("image/*");
                self.imgLabel = ko.observable();
                self.uploaded = ko.observable(false);
                self.uniqueTokenForUpload = undefined;
                self.uploadSuccessCnt = ko.observable(0);
                self.uploadFailCnt = ko.observable(0);
                self.configType = ko.observable();

                self.labelsToTrain = ko.observableArray([]);
                self.trainSteps = ko.observable(100);
                self.tensorflowHubModule = ko.observable("");
                self.showTrainOptions = ko.observable();

                self.modelDataProvider = ko.observable();
                self.modelSelectedForRestore = ko.observable();

                self.selectListener = function (event) {
                    self.clearFiles(self.uploaded());
                    var files = event.detail.files;
                    if (files.length > 0 && files[0].type.startsWith("image/")) {
                        for (var i = 0, len = files.length; i < len; i++) { //for multiple files          
                            (function (file) {
                                var name = file.name;
                                var reader = new FileReader();
                                reader.onload = function (e) {
                                    // push file content  
                                    self.fileList.push({name: name, src: e.target.result, uploaded: ko.observable(""), file: file});
                                };
                                reader.readAsDataURL(file);
                            })(files[i]);
                        }
                        //document.getElementById('upload-button').disabled = false;
                    } else {
                        self.errorMessage("Please choose image files. The ones that have a file extn like .jpg or .png or .jpeg");
                    }
                };

                self.clearFiles = function (init) {
                    //document.getElementById('upload-button').disabled = true;
                    self.fileName(null);
                    self.file(null);
                    self.fileList([]);
                    self.errorMessage(null);
                    self.uploaded(false);
                    self.uploadSuccessCnt(0);
                    self.uploadFailCnt(0);
                    self.inprogress("");
                    self.uniqueTokenForUpload = undefined;
                    if (init) {
                        self.imgLabel("");
                    }
                };

                self.acceptArr = ko.pureComputed(function () {
                    var accept = self.acceptStr();
                    return accept ? accept.split(",") : [];
                }, self);

                self.uploadImages = function () {
                    self.inprogress("YES");
                    document.getElementById('upload-button').disabled = true;
                    client.invokeGet(app.hostBasePath() + '/uniqueId')
                            .done(function (data) {
                                if (data.UUID) {
                                    console.log("unique id is :" + data.UUID);
                                    self.uniqueTokenForUpload = data.UUID;
                                    console.log("Starting upload of files" + self.fileList().length);
                                    self.doUpload();
                                } else {
                                    self.errorMessage("Something went wrong.");
                                    self.inprogress("");
                                }
                            })
                            .fail(function (xhr, status, error) {
                                self.errorMessage("Could not upload the images. Something went wrong.");
                                self.inprogress("");
                                document.getElementById('upload-button').disabled = false;
                            });
                };

                self.doUpload = function () {
                    for (var i = 0, len = self.fileList().length; i < len; i++) { //for multiple files          
                        (function (file, index) {
                            var reader = new FileReader();
                            reader.onloadend = function (e) {
                                var fileContent = self.contentToBase64(reader.result);
                                client.invokePost(app.hostBasePath() + '/admin/upload/trainingimg',
                                        {label: self.imgLabel(), uploadToken: self.uniqueTokenForUpload, fileName: file.name, content: fileContent, userName: app.userLogin(), mimeType: file.type, size: file.size})
                                        .done(function (data) {
                                            if (data && data.id && data.id > 0) {
                                                self.fileList()[index].uploaded("YES");
                                                self.uploadSuccessCnt(self.uploadSuccessCnt() + 1);
                                            } else {
                                                self.fileList()[index].uploaded("FAIL");
                                                self.uploadFailCnt(self.uploadFailCnt() + 1);
                                            }
                                        })
                                        .fail(function (xhr, status, error) {
                                            self.fileList()[index].uploaded("FAIL");
                                            self.uploadFailCnt(self.uploadFailCnt() + 1);
                                        })
                                        .always(function () {
                                            if ((self.uploadFailCnt() + self.uploadSuccessCnt()) == self.fileList().length) {
                                                self.inprogress("DONE");
                                            }
                                        });
                            };
                            reader.readAsArrayBuffer(file);
                        })(self.fileList()[i].file, i);
                    }
                    self.uploaded(true);
                };

                self.contentToBase64 = function (content) {
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
                };

                self.getLabelToTrain = function () {
                    self.inprogress("YES");
                    client.invokeGet(app.hostBasePath() + '/admin/labelToTrain')
                            .done(function (data) {
                                if (data && data.length > 0) {
                                    for (var i = 0, max = 10; i < max; i++) {
                                        self.labelsToTrain(data);
                                    }
                                } else {
                                    self.labelsToTrain("");
                                    self.errorMessage("There are no labels to train.");
                                }
                                self.inprogress("");
                            })
                            .fail(function (xhr, status, error) {
                                self.errorMessage("Could not upload the images. Something went wrong.");
                                self.inprogress("");
                                document.getElementById('upload-button').disabled = false;
                            });
                };

                self.goToUploadImages = function () {
                    self.errorMessage("");
                    self.selectedTab("upload-train-image");
                };

                self.configTypeChanged = function (event) {
                    console.log(event.detail.value);
                    if (event.detail.value === "manual") {
                        self.getLabelToTrain();
                    }

                };

                self.manualTrainModel = function () {
                    console.log("In Manual training...");
                    self.inprogress("YES");
                    document.getElementById('manual-train-button').disabled = false;
                    client.invokePost(app.hostBasePath() + '/admin/startTraining',
                            {userName: app.userLogin(), tfHubModule: self.tensorflowHubModule(), trainSteps: self.trainSteps()})
                            .done(function (data) {
                                if (data && data.incepTo && data.incepTo.id && data.incepTo.id > 0) {
                                    self.inprogress("DONE");
                                } else {
                                    self.inprogress("FAIL");
                                }
                            })
                            .fail(function (xhr, status, error) {
                                self.errorMessage("Could not upload the images. Something went wrong.");
                                self.inprogress("");
                                document.getElementById('manual-train-button').disabled = false;
                            });

                };
            }

            /*
             * Returns a constructor for the ViewModel so that the ViewModel is constructed
             * each time the view is displayed.  Return an instance of the ViewModel if
             * only one instance of the ViewModel is needed.
             */
            return new settingsViewModel();
        }
);
