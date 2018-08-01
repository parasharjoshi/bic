/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * home module
 */
define(['ojs/ojcore', 'knockout', 'appController', 'restClient', 'ojs/ojchart', 'ojs/ojradioset', 'ojs/ojprogress'
], function (oj, ko, app, client) {
    /**
     * The view model for the main content view template
     */
    function homeContentViewModel() {
        var self = this;
        self.userFullName = app.userFullName();
        self.threeDValue = ko.observable('off');
        self.initializing = ko.observable();
        self.recogResultPieSeries = ko.observableArray();
        self.requestPieSeries = ko.observableArray();
        self.progressValue = ko.observable(-1);
        self.selectionValue = ko.observableArray();
        this.errorMessage = ko.observable();

        self.connected = function () {
            console.log("Initializing app...");
            self.clearVars();
            self.initializeCharts();
        };

        self.clearVars = function () {
            self.userFullName = app.userFullName();
            self.initializing(true);
            self.recogResultPieSeries(undefined);
            self.requestPieSeries(undefined);
            self.selectionValue(undefined);
            this.errorMessage(undefined);
        };

        self.initializeCharts = function () {
            console.log("initializeCharts...");
            self.progressValue(-1);
            self.initializing(true);
            client.invokeGet(app.hostBasePath() + '/recogrequests')
                    .done(function (data) {
                        console.log("done...");
                        self.createCharts(data);
                    })
                    .fail(function (xhr, status, error) {
                        console.log("fail...");
                        self.initializing(false);
                        self.errorMessage("Initialation failed. Could not load data. Please contact admin if problem persists.");
                    });
        };

        self.createCharts = function (data) {
            var correctRecog = 0;
            var incorrectRecog = 0;
            var unknownRecog = 0;
            var recognized = 0;
            var failedRecog = 0;

            data.forEach(
                    function (item, i) {
                        if (item.userVote !== null) {
                            if (item.userVote === true) {
                                correctRecog++;
                            } else if (item.userVote === false) {
                                incorrectRecog++;
                            }
                        } else {
                            unknownRecog++;
                        }
                        if (item.recognizedObject !== null) {
                            recognized++;
                        } else {
                            failedRecog++;
                        }
                    });
            var recogResults = [{name: "Correctly recognized", items: [{value: correctRecog, id: 's1'}]},
                {name: "Incorrect Recognition", items: [{value: incorrectRecog, id: 's2'}]},
                {name: "Unknown", items: [{value: unknownRecog, id: 's3'}]}];
            self.recogResultPieSeries(recogResults);

            self.recogResultPieSeries = ko.computed(function () {
                recogResults[1]['color'] = '#FF5733';
                recogResults[0]['color'] = '#2ECC71';
                recogResults[2]['color'] = '#D5D8DC';
                return recogResults;
            });

            var requests = [{name: "Recognized", items: [recognized]},
                {name: "Unrecognized", items: [failedRecog]}];
            self.requestPieSeries(requests);

            self.requestPieSeries = ko.computed(function () {
                requests[1]['color'] = '#FF5733';
                requests[0]['color'] = '#2ECC71';
                return requests;
            });

            self.initializing(false);
        };
    }

    return homeContentViewModel;
});
