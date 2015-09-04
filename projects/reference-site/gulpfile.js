var gulp = require('gulp'),
    selenium = require('selenium-standalone'),
    nightwatch = require('gulp-nightwatch');

function errorLog(error){
  console.error.bind(error);
  this.emit('end');
}

var args = process.argv.slice(2);
var seleniumHost;

if (args.length > 1){
  seleniumHost = args[1].replace('-', '');
}

gulp.task('selenium-connect', function (done) {

  if (!seleniumHost){

    selenium.install({
      logger: function (message) { }
    }, function (err) {
      if (err) return done(err);

      selenium.start(function (err, child) {
        if (err) return done(err);
        selenium.child = child;
        done();
      });
    });

  } else {

    done();

  }

});


gulp.task('tests', ['selenium-connect'], function() {
  var hostToConnect = seleniumHost ? seleniumHost : '127.0.0.1'


  var nightwatchConfig = {
    cliArgs: {
        env: 'chrome',
        selenium_host: hostToConnect
      }
  };

  console.log(nightwatchConfig);

  return gulp.src('')
    .pipe(nightwatch(nightwatchConfig))
    .on('error', errorLog);
});

gulp.task('e2e', ['tests'], function () {

  if (!seleniumHost){
    selenium.child.kill();
  }

});


