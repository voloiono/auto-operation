const fs = require('fs');
const flow = JSON.parse(fs.readFileSync('flow9.json', 'utf8'));
const req = {
  script: flow.generatedScript,
  platform: 'windows',
  name: 'login_auto'
};
console.log(JSON.stringify(req));
