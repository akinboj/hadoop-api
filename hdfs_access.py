import pyarrow as pa
import pyarrow.fs as pafs

def main():
    try:
        # Specify the Kerberos authentication details
        hdfs = pafs.HadoopFileSystem(
            host='pegacorn-fhirplace-namenode-0.pegacorn-fhirplace-namenode.site-a.svc.cluster.local',  # Replace with your actual namenode host
            port=8020,  # Common default port for HDFS namenode
            user='myapp/pegacorn-fhirplace-bigdata-api-0.pegacorn-fhirplace-bigdata-api.site-a.svc.cluster.local@PEGACORN-FHIRPLACE-AUDIT.LOCAL',  # Include the realm if necessary
            kerb_ticket='/tmp/krb5cc_0',  # Typically /tmp/krb5cc_0
            #driver='libhdfs3'  # Use 'libhdfs3' for the libhdfs3 backend if installed
        )

        # Example: Listing a directory
        info = hdfs.get_file_info(pafs.FileSelector('/user/path/to/list', recursive=False))
        print("Directory contents:")
        for file_info in info:
            print(f'Name: {file_info.path} Size: {file_info.size}')

        # Example: Writing to a file
        with hdfs.open_output_stream('/user/path/to/test.txt') as f:
            f.write(b"Hello, HDFS!")

        print("File written successfully.")

    except Exception as e:
        print(f"An error occurred: {e}")

if __name__ == "__main__":
    main()
