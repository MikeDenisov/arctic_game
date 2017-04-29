using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Domain.Core;

namespace Domain.Entities
{
    [Table("users")]
    public class User : IEntity<long>
    {
        [Key]        
        public long Key { get; set; }

        [Required]
        public string Name { get; set; }

        public ICollection<Photo> Photos { get; set; }
    }
}
