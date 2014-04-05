/** google global namespace for Google projects. */
var google = google || {};

/** appengine namespace for Google Developer Relations projects. */
google.appengine = google.appengine || {};

/** samples namespace for App Engine sample code. */
google.appengine.rockpaperscissorslizardspock = google.appengine.rockpaperscissorslizardspock || {};

/** hello namespace for this sample. */
google.appengine.rockpaperscissorslizardspock.rockPaperScissorsLizardSpock = google.appengine.rockpaperscissorslizardspock.rockPaperScissorsLizardSpock || {};

/**
 * Initializes the application.
 * @param {string} apiRoot Root of the API's path.
 */
google.appengine.samples.hello.init = function(apiRoot) {
  // Loads the OAuth and helloworld APIs asynchronously, and triggers login
  // when they have completed.
  var apisToLoad;
  var callback = function() {
    if (--apisToLoad == 0) {
      google.appengine.rockpaperscissorslizardspock.rockpaperscissorslizardspock.enableButtons();
    }
  }

  apisToLoad = 1; // must match number of calls to gapi.client.load()
  gapi.client.load('rockpaperscissorslizardspock', 'v1', callback, apiRoot);
};