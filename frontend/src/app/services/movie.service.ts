export const MovieService = {
  async list() {
    const res = await fetch('/api/movies');
    if (!res.ok) throw new Error('Failed to load movies');
    return res.json();
  },
  async get(id: number) {
    const res = await fetch(`/api/movies/${id}`);
    if (!res.ok) throw new Error('Failed to load movie');
    return res.json();
  }
};
