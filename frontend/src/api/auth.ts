import client from "./client";

export interface CurrentUser {
  id: string;
  email: string;
  name: string;
}

export const authApi = {
  me: () => client.get<CurrentUser>("/me").then((r) => r.data),
  logout: () =>
    client.post("/logout", {}, { baseURL: "/" }).then(() => {}),
};
