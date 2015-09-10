var gulp = require('gulp'),
    selenium = require('selenium-standalone'),
    nightwatch = require('gulp-nightwatch');

function errorLog(error){
  console.error.bind(error);
  this.emit('end');
}

gulp.task('selenium-connect', function (done) {
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

});

gulp.task('run-nightwatch', ['selenium-connect'], function() {
  return gulp.src('')
    .pipe(nightwatch())
    .on('error', errorLog);
});

gulp.task('functional-test', ['run-nightwatch'], function () {
  selenium.child.kill();
});


