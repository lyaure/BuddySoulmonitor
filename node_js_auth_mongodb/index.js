// Import package
var mongodb = require('mongodb');
var ObjectID = mongodb.ObjectID;
var crypto = require('crypto');
var express = require('express');
var bodyParser = require('body-parser');
var nodemailer = require('nodemailer');
var jwt = require('jsonwebtoken');
var ObjectId = require('mongodb').ObjectID;
require("dotenv").config();

const IP_ADDRESS = '192.168.14.183'
const EMAIL_SECRET = 'asdf1093KMnzxcvnkljvasdu09123nlasdasdf';

const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.user,
        pass: process.env.pass,
    },
    tls: {
        rejectUnauthorized: false
    }
});

//PASSWORD UTILS
//CREATE FUNCTION TO RANDOM SALT
var getRandomString = function (length) {
    return crypto.randomBytes(Math.ceil(length / 2))
        .toString('hex') /* convert to hexa format */
        .slice(0, length);
};

var sha512 = function (password, salt) {
    var hash = crypto.createHmac('sha512', salt);
    hash.update(password);
    var value = hash.digest('hex');
    return {
        salt: salt,
        passwordHash: value
    };
};

function saltHashPassword(userPassword) {
    var salt = getRandomString(16); // create 16 random character
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function checkHashPassword(userPassword, salt) {
    var passwordData = sha512(userPassword, salt);
    return passwordData;
}

function sendMail(email, subject, html) {
    transporter.sendMail({
        from: 'buddynsoulmonitor@gmail.com',
        to: email,
        subject: subject,
        html: html
    });
}

//Create Express Service
var app = express();
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: true}));

//Create MongoDB Client
var MongoClient = mongodb.MongoClient;

//Connection URL
var url = 'mongodb://localhost:27017' //27017 is default port

MongoClient.connect(url, {useNewUrlParser: true}, function (err, client) {
    if (err)
        console.log('Unable to connect to the mongoDB server.Error', err);
    else {

        //Register
        app.post('/register', (request, response, next) => {
            var post_data = request.body;

            var plaint_password = post_data.password;
            var hash_data = saltHashPassword(plaint_password);

            var password = hash_data.passwordHash; // Save password hash
            var salt = hash_data.salt;

            var name = post_data.name;
            var email = post_data.email;

            var insertJson = {
                'email': email,
                'password': password,
                'salt': salt,
                'name': name,
                'confirmed': false
            };
            var db = client.db('buddy&soulmonitor');

            //Check exists email
            db.collection('user')
                .find({'email': email}).count(function (err, number) {
                if (number != 0) {
                    response.json('Email already exists');
                    console.log('Email already exists');
                } else {
                    //Insert data
                    db.collection('user')
                        .insertOne(insertJson, function (error, res) {
                            response.json('Registration success');
                            console.log('Registration success');
                        })

                    //send confirmation mail
                    db.collection('user').findOne({'email': email}, function (err, user) {
                        if (err) {
                            console.log(err)
                            response.json(err);
                        } else {
                            // async email
                            jwt.sign(
                                {
                                    userId: user._id,
                                    //email: email,
                                },
                                EMAIL_SECRET,
                                {
                                    expiresIn: '1d',
                                },
                                (err, emailToken) => {
                                    //const url = `http://localhost:3000/confirmation/${emailToken}`;
                                    //const url = `http://192.168.14.183:3000/confirmation/${emailToken}`;
                                    const url = `http://${IP_ADDRESS}:3000/confirmation/${emailToken}`;

                                    var subject = 'Confirm you registration to Buddy&Soul Monitor';
                                    var html = `Please click this email to confirm your email: <a href="${url}">${url}</a>`;

                                    sendMail(email, subject, html);
                                },
                            );
                        }
                    })
                }
            })
        });

        //Login
        app.post('/login', async (request, response, next) => {
            var post_data = request.body;

            var email = post_data.email;
            var userPassword = post_data.password;

            var db = client.db('buddy&soulmonitor');

            //Check exists email
            db.collection('user')
                .find({'email': email}).count(function (err, number) {
                if (number == 0) {
                    response.json('Email not exists');
                    console.log('Email not exists');
                } else {
                    //Insert data
                    db.collection('user')
                        .findOne({'email': email}, function (err, user) {
                            var salt = user.salt; // Get salt from user
                            var hashed_password = checkHashPassword(userPassword, salt).passwordHash; // Get password from user
                            var encrypted_password = user.password;
                            if (hashed_password == encrypted_password) {
                                response.json('Login success');
                                console.log('Login success');
                            } else {
                                response.json('Wrong password');
                                console.log('Wrong password');

                            }
                        })
                }
            })
        });

        //Email confirmation
        app.get('/confirmation/:token', (request, response, next) => {

            try {
                const decoded = jwt.verify(request.params.token, EMAIL_SECRET);
                var userId = decoded.userId
                console.log('userId:' + userId);

                var db = client.db('buddy&soulmonitor');

                db.collection('user')
                    .findOne({'_id': ObjectId(userId)}, function (err, user) {
                        if (err) {
                            console.log(err);
                            response.json('Error in mail confirmation');
                        } else {
                            if (user.confirmed) {
                                console.log('Mail has been already confirmed');
                                response.json('Mail has been already confirmed');
                            } else {
                                db.collection('user')
                                    .updateOne({'_id': ObjectId(userId)}, //filter
                                        {$set: {'confirmed': true}}
                                    ).then(() => {
                                    console.log("Db updated");
                                })
                                    .catch((err) => {
                                        console.log(err);
                                    })

                                console.log('Mail confirmed');
                                response.json('Mail confirmed');
                            }

                        }
                    })


            } catch (e) {
                console.log(e);
                response.json('error');
            }

            //return res.redirect('http://localhost:3001/login');
        });

        //Start Web Server
        app.listen(3000, () => {
            console.log('Connected to MongoDB Server, WebService running on port 3000');
        })
    }
})