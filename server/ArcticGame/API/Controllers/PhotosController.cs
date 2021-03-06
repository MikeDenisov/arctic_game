﻿using System;
using System.Collections.Generic;
using System.Configuration;
using System.Data.Entity;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Reflection;
using System.Threading.Tasks;
using System.Web;
using System.Web.Hosting;
using System.Web.Http;
using API.Models;
using API.Providers;
using Domain.Core;
using Domain.Entities;
using Microsoft.Azure;
using Microsoft.WindowsAzure.Storage;
using Microsoft.WindowsAzure.Storage.Auth;
using Microsoft.WindowsAzure.Storage.Blob;
using MimeTypes;

namespace API.Controllers
{
    public class PhotosController : ApiController
    {
        private readonly IEntityRepository<Photo, long> _photosRepository;
        private readonly IEntityRepository<User, long> _usersRepository;
        private const string Container = "photos";

        public PhotosController(IEntityRepository<Photo, long> photosRepository, IEntityRepository<User, long> usersRepository)
        {
            _photosRepository = photosRepository;
            _usersRepository = usersRepository;
        }

        [Route("~/api/photos/upload")]
        [HttpPost]
        [AllowAnonymous]
        public async Task<HttpResponseMessage> Post(
            [FromUri] string username,
            [FromUri] string comment,
            [FromUri] double lon,
            [FromUri] double lat,
            [FromUri] double az)//UploadPhotoDto dto)
        {
            //TODO Add authorization
            if (string.IsNullOrEmpty(username))
                return new HttpResponseMessage(HttpStatusCode.BadRequest);

            var user = _usersRepository.FindBy(u => u.Name == username).FirstOrDefault();
            if (user == null)
            {
                _usersRepository.Add(new User()
                {
                    Name = username
                });
                _usersRepository.Save();
                user = _usersRepository.FindBy(u => u.Name == username).FirstOrDefault();
                if (user == null)
                    return new HttpResponseMessage(HttpStatusCode.InternalServerError);
            }
            //

            var result = new HttpResponseMessage(HttpStatusCode.OK);
            if (Request.Content.IsMimeMultipartContent())
            {
                await Request.Content.ReadAsMultipartAsync<MultipartMemoryStreamProvider>(new MultipartMemoryStreamProvider()).ContinueWith((task) =>
                {
                    // Retrieve storage account from connection string.
                    CloudStorageAccount storageAccount = CloudStorageAccount.Parse(
                        CloudConfigurationManager.GetSetting("StorageConnectionString"));

                    // Create the blob client.
                    CloudBlobClient blobClient = storageAccount.CreateCloudBlobClient();

                    // Retrieve reference to a previously created container.
                    CloudBlobContainer container = blobClient.GetContainerReference(Container);                    

                    var provider = task.Result;
                    foreach (var content in provider.Contents)
                    {
                        
                        //var image = Image.FromStream(stream);
                        //var filePath = GetFileStoragePath();
                        var fileName = Guid.NewGuid() + ".jpg";
                        //var fullPath = Path.Combine(filePath, fileName);

                        _photosRepository.Add(new Photo()
                        {
                            UserKey = user.Key,
                            LocalPath = fileName,
                            TimeStamp = DateTime.Now,
                            Location = new Location()
                            {
                                Latitude = lat,
                                Longtitude = lon,
                                Azimuth = az
                            },
                            Comment = comment
                        });

                        // Retrieve reference to a blob named "myblob".
                        CloudBlockBlob blockBlob = container.GetBlockBlobReference(fileName);

                        //image.st
                        // Create or overwrite the "myblob" blob with contents from a local file.
                        //using (var fileStream = System.IO.File.OpenRead(@"path\myfile"))
                        //{
                        var stream = content.ReadAsStreamAsync().Result;//ReadAsStreamAsync().Result;
                        blockBlob.UploadFromStream(stream);
                        //}

                        //using (var m = new FileStream(fullPath, FileMode.Create))
                        //{
                        //    image.Save(m, image.RawFormat);
                        //}

                        _photosRepository.Save();
                    }
                });
                return result;
            }
            else
            {
                throw new HttpResponseException(Request.CreateResponse(HttpStatusCode.NotAcceptable, "This request is not properly formatted"));
            }
        }

        [Route("~/api/photos/{id}")]
        [HttpGet]
        [AllowAnonymous]
        public HttpResponseMessage Get(long id)
        {
            var imageData = _photosRepository.FindBy(p => p.Key == id).FirstOrDefault();
            if (imageData == null || string.IsNullOrEmpty(imageData.LocalPath))
                return new HttpResponseMessage(HttpStatusCode.NotFound);

            CloudStorageAccount storageAccount = CloudStorageAccount.Parse(
                        CloudConfigurationManager.GetSetting("StorageConnectionString"));

            CloudBlobClient blobClient = storageAccount.CreateCloudBlobClient();

            CloudBlobContainer imagesContainer = blobClient.GetContainerReference(Container);
            CloudBlockBlob blockBlob = imagesContainer.GetBlockBlobReference(imageData.LocalPath);

            // Save blob contents to a file.
            using (var downloadStream = new MemoryStream())
            {
                blockBlob.DownloadToStream(downloadStream);
                
                //var result = new HttpResponseMessage(HttpStatusCode.OK);
                //result.Content = new ByteArrayContent(downloadStream.ToArray());
                var image = Image.FromStream(downloadStream);

                using (var memStream = new MemoryStream())
                {
                    image.Save(memStream, image.RawFormat);

                    var result = new HttpResponseMessage(HttpStatusCode.OK);

                    result.Content = new ByteArrayContent(memStream.ToArray());
                    
                    var extension = imageData.LocalPath.Split('.').Last();

                    result.Content.Headers.ContentType = new MediaTypeHeaderValue(MimeTypeMap.GetMimeType(extension));

                    return result;
                }
            }
        }

        [Route("~/api/photos")]
        [HttpGet]
        [AllowAnonymous]
        public List<PhotoData> GetAll()
        {
            var result = new List<PhotoData>();
            var photos = _photosRepository.GetAll().ToList();
            var users = _usersRepository.GetAll().ToList();
            foreach (var photo in photos)
            {
                result.Add(new PhotoData()
                {
                    Location = photo.Location,
                    Key = photo.Key,
                    TimeStamp = photo.TimeStamp,
                    Comment = photo.Comment,
                    Owner = photo.User.Name
                });
            }

            return result;
        }

        private static string GetFileStoragePath()
        {
            var path = HostingEnvironment.MapPath("~/Photos");
            if (path == null)
            {
                var uriPath = Path.GetDirectoryName(Assembly.GetExecutingAssembly().GetName().CodeBase);
                path = new Uri(uriPath).LocalPath + "/Photos";
            }
            return path;
        }
    }
}
