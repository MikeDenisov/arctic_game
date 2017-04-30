using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using Domain.Entities;

namespace API.Models
{
    public class UploadPhotoDto
    {
        public string UserName { get; set; }
        public Location Location { get; set; }
        public string Comment { get; set; }
    }

    public class PhotoData
    {
        public long Key { get; set; }
        public string Owner { get; set; }
        public Location Location { get; set; }
        public string Comment { get; set; }
        public DateTime TimeStamp { get; set; }
    }
}