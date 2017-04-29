using System;
using System.Collections.Generic;
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
using Domain.Core;
using Domain.Entities;
using MimeTypes;

namespace API.Controllers
{
    public class PhotosController : ApiController
    {
        private readonly IEntityRepository<Photo, long> _photosRepository;
        private readonly IEntityRepository<User, long> _usersRepository;

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
                    var provider = task.Result;
                    foreach (var content in provider.Contents)
                    {
                        var stream = content.ReadAsStreamAsync().Result;
                        var image = Image.FromStream(stream);
                        var testName = content.Headers.ContentDisposition.Name;
                        var filePath = GetFileStoragePath();
                        var fileName = Guid.NewGuid() + ".jpg";
                        var fullPath = Path.Combine(filePath, fileName);

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

                        using (var m = new FileStream(fullPath, FileMode.Create))
                        {
                            image.Save(m, image.RawFormat);
                        }

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
            try
            {
                var imageData = _photosRepository.FindBy(p => p.Key == id).FirstOrDefault();//GetSingle(id);
                if (imageData == null || string.IsNullOrEmpty(imageData.LocalPath))
                    return new HttpResponseMessage(HttpStatusCode.NotFound);

                var storagePath = GetFileStoragePath();
                var fullPath = Path.Combine(storagePath, imageData.LocalPath);

                var fileStream = new FileStream(fullPath, FileMode.Open);
                var image = Image.FromStream(fileStream);
                var memoryStream = new MemoryStream();
                image.Save(memoryStream, image.RawFormat);

                var result = new HttpResponseMessage(HttpStatusCode.OK);

                result.Content = new ByteArrayContent(memoryStream.ToArray());

                var extension = imageData.LocalPath.Split('.').Last();

                result.Content.Headers.ContentType = new MediaTypeHeaderValue(MimeTypeMap.GetMimeType(extension));

                return result;
            }
            catch (Exception)
            {
                return new HttpResponseMessage(HttpStatusCode.InternalServerError);
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
