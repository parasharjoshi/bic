/**
 * @license
 * Copyright (c) 2014, 2018, Oracle and/or its affiliates.
 * The Universal Permissive License (UPL), Version 1.0
 */
/*
 * Your incidents ViewModel code goes here
 */
define(['ojs/ojcore', 'knockout', 'jquery', 'appController', 'restClient', 'ojs/ojfilepicker', 'ojs/ojradioset', 'ojs/ojprogress'],
        function (oj, ko, $, app, client) {

            function recognizeViewModel() {
                var self = this;
                self.acceptStr = ko.observable("image/*");
                self.uploaded = ko.observable(false);
                self.resultRetrieved = ko.observable(false);
                self.feedbackValue = ko.observable(undefined);
                self.feedbackProvided = ko.observable(false);
                self.inLoadingState = ko.observable(false);
                self.initializing = ko.observable();
                self.progressValue = ko.observable(-1);
                self.recogEnabled = ko.observable(undefined);
                self.request = ko.observable(undefined);

                self.enableFeedbackButton = ko.computed(function () {
                    return !(self.feedbackValue());
                }, this);


                self.connected = function () {
                    // Implement if needed
                    self.clearFiles();
                    self.initialChecks();
                };

                self.disconnected = function () {
                    // Implement if needed
                };

                self.transitionCompleted = function () {
                    // Implement if needed
                };

                self.initialChecks = function () {
                    self.initializing(true);
                    self.recogEnabled(false);
                    client.invokeGet(app.hostBasePath() + '/canrecognize')
                            .done(function (data) {
                                if (data.frameworkReady) {
                                    self.recogEnabled(data.frameworkReady);
                                } else {
                                    self.recogEnabled(data.frameworkReady);
                                    self.errorMessage("Initialization failed. Server returned with a negative response for recognition model availability.");
                                }
                                self.initializing(false);
                            })
                            .fail(function (xhr, status, error) {
                                self.initializing(false);
                                self.errorMessage("Initial checks failed. Cannot proceed with recognition.");
                            });
                };

                self.fileName = ko.observable();
                self.fileSrc = ko.observable();
                self.file = ko.observable();
                self.recognizedObj = ko.observable();
                self.resultProbability = ko.observable();
                self.errorMessage = ko.observable();
                self.reqTo = ko.observable();

                self.selectListener = function (event) {
                    self.clearFiles();
                    var files = event.detail.files;
                    self.fileName(null);
                    if (files.length === 1 && files[0].type.startsWith("image/")) {
                        self.file(files[0]);
                        self.fileName(files[0].name);

                        var reader = new FileReader();

                        // Closure to capture the file information.
                        reader.onload = (function () {
                            return function (e) {
                                self.fileSrc(e.target.result);
                            };
                        })(files[0]);
                        // Read in the image file as a data URL.
                        reader.readAsDataURL(files[0]);
                        document.getElementById('recog-button').disabled = false;
                    } else{
                        self.errorMessage("Please choose an image file. The ones that have a file extn like .jpg or .png or .jpeg");
                    }
                };

                self.clearFiles = function () {
                    document.getElementById('recog-button').disabled = true;
                    self.fileName(null);
                    self.file(null);
                    self.fileSrc(null);
                    self.uploaded(false);
                    self.feedbackProvided(false);
                    self.resultRetrieved(false);
                    self.recognizedObj(null);
                    self.resultProbability(null);
                    self.errorMessage(null);
                    self.reqTo(null);
                    self.feedbackValue(null);
                };

                self.acceptArr = ko.pureComputed(function () {
                    var accept = self.acceptStr();
                    return accept ? accept.split(",") : [];
                }, self);


                self.uploadAndRecog = function () {
                    self.inLoadingState(true);
                    document.getElementById('recog-button').disabled = true;
                    var reader = new FileReader();
                    reader.onloadend = function (e) {
                        var fileContent = self.contentToBase64(reader.result);
                        //var fileContent = reader.result;

                        //self.fileSrc2("data:" + self.file().type + ";base64," + fileContent);
                        //self.errorMessage("Please wait...!");
                        client.invokePost(app.hostBasePath() + '/recognize',
                                {fileName: self.file().name, content: fileContent, userName: app.userLogin(), mimeType: self.file().type, size: self.file().size})
                                .done(function (data) {
                                    self.handleSuccess(data);
                                })
                                .fail(function (xhr, status, error) {
                                    self.handleFailure(xhr, status, error);
                                });
                    };
                    reader.readAsArrayBuffer(self.file());
                    self.uploaded(true);
                };

                self.submitFeedback = function () {
                    self.feedbackProvided(true);
                    if (self.feedbackValue() !== "unknown") {
                        client.invokePost(app.hostBasePath() + '/recogFeedback',
                                {id: self.reqTo().id, userVote: self.feedbackValue()})
                                .done(function (data) {
                                    console.log(data);
                                })
                                .fail(function (xhr, status, error) {
                                });
                    }
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

                self.handleSuccess = function (data) {
                    self.resultRetrieved(true);
                    self.errorMessage(null);
                    self.reqTo(data);
                    if (data.error && data.error.errorCode) {
                        self.errorMessage(data.error.errorCode + ' : ' + data.error.errorMessage);
                    } else if (data) {
                        if (data.recognizedObject) {
                            self.recognizedObj(data.recognizedObject);
                        }
                        if (data.probability) {
                            self.resultProbability(data.probability);
                        }
                    } else {
                        self.errorMessage("Something went wrong. Please try later..!");
                    }
                    self.inLoadingState(false);
                };

                self.handleFailure = function (xhr, status, error) {
                    self.resultRetrieved(true);
                    self.errorMessage(null);
                    if (xhr.status && xhr.statusText) {
                        self.errorMessage(xhr.status + ' : ' + xhr.statusText + " - Something went wrong. Please try later..!");
                    } else {
                        self.errorMessage("Something went wrong. Please try later..!");
                    }
                    self.inLoadingState(false);
                };
            }

            /*
             * Returns a constructor for the ViewModel so that the ViewModel is constructed
             * each time the view is displayed.  Return an instance of the ViewModel if
             * only one instance of the ViewModel is needed.
             */
            return new recognizeViewModel();
        }
);
