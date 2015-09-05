// Cloud Code methods:
Parse.Cloud.define('getBeaconGoals', function(request, response) {
    verifyBeacon(request.params.beaconId).then(function(results) {
        return getGoals(request.params.beaconId, 'start');
    }).then(function(results) {
        response.success(results);
    }, function(error) {
        response.error(error);
    });
});

Parse.Cloud.define('onBeaconReached', function(request, response) {
    var values;
    verifyBeacon(request.params.beaconId).then(function(results) {
        return updateUserJourney(request);
    }).then(function(results) {
        return getGoals(request.params.beaconId, 'end');
    }).then(function(results) {
        return calculatePoints(results);
    }).then(function(results) {
        values = results;
        Parse.Cloud.useMasterKey();
        var q = new Parse.Query(Parse.User);
        return q.get('JUITjX0UXr').then(function(results) {
            results.increment('points', values.points);
            return results.save();
        });
    }).then(function(results) {
        response.success(values);
    }, function(error) {
        response.error(error);
    });
});

// server-side code:
function getGoals(beaconId, key) {
    var Goal = Parse.Object.extend('Goal'),
        q    = new Parse.Query(Goal);

    q.equalTo(key, beaconId);
    return q.find({
        success: function(results) {
            if (results.length > 0) {
                return Parse.Promise.as(results);
            } else {
                return Parse.Promise.error('no results');
            }
        },
        error: function(error) {
            console.error('query returned error');
            console.error(error);
            return Parse.Promise.error(error);
        }
    });
}

function verifyBeacon(beaconId) {
    var Beacon = Parse.Object.extend('Beacon'),
        q      = new Parse.Query('Beacon'),
        valid  = [];

    console.log('beaconId: ' + beaconId);
    q.equalTo('beaconId', beaconId);
    return q.find({
        success: function(results) {
            if (results.length > 0) {
                return Parse.Promise.as(results);
            } else {
                return Parse.Promise.error('beacon invalid');
            }
        },
        error: function(error) {
            return Parse.Promise.error(error);
        }
    });
}

function updateUserJourney(request) {
    var UserJourney = Parse.Object.extend('UserJourney'),
        uj          = new UserJourney();

    return uj.save({
        beaconId: request.params.beaconId,
        user: request.params.user,
        timestamp: new Date().getTime()
    }).then(function(object) {
        console.log('UserJourney updated');
        return Parse.Promise.as('UserJourney updated');
    }, function(error) {
        console.error(error);
        return Parse.Promise.error(error);
    });
}

function calculatePoints(goals) {
    var UserJourney = Parse.Object.extend('UserJourney'),
        q           = new Parse.Query(UserJourney),
        currentGoal = {goal: goals[0]},
        best        = goals[0].get('bestTime'),
        max         = goals[0].get('maxPoints');

    q.descending('createdAt');
    q.limit(2);
    return q.find().then(function(results) {
        return Parse.Promise.as(Math.abs(results[0].get('timestamp') - results[1].get('timestamp')) / 1000);
    }).then(function(results) {
        if (results <= best) {
            currentGoal.points = max;
        } else {
            currentGoal.points = parseInt(best / results * max);
        }
        return Parse.Promise.as(currentGoal);
    });
}
