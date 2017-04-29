using System;
using System.Collections.Generic;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using System.Threading.Tasks;

namespace Domain.Core
{
    public interface IEntityRepository<T, TKey> 
        where T : class, IEntity<TKey>, new()
        where TKey : IComparable
    {

        IQueryable<T> AllIncluding(params Expression<Func<T, object>>[] includeProperties);
        IQueryable<T> GetAll();
        T GetSingle(TKey key);
        IQueryable<T> FindBy(Expression<Func<T, bool>> predicate);

        void Add(T entity);
        void Edit(T entity);
        void Delete(T entity);
        void DeleteGraph(T entity);
        void Save();
    }
}
