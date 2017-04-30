using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Domain.Core;

namespace Domain.Entities
{
    [Table("photos")]
    public class Photo : IEntity<long>
    {
        [Key]
        public long Key { get; set; }
        
        [Required]
        public long UserKey { get; set; }

        [Required]
        public string LocalPath { get; set; }

        public string Comment { get; set; }

        public Location Location { get; set; }

        public DateTime TimeStamp { get; set; }
        [ForeignKey("UserKey")]
        public User User { get; set; }
    }
}
